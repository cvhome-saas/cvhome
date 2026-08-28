import {Injectable, computed, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {DestroyRef} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {BillingApi} from '../services/billing.api.service';
import {ApiErrorService} from '@core/errors/api-error.service';
import {SubscriptionFacade} from '@layouts/console-shell/billing/subscription.facade';
import {ToastService} from '@shared/ui/toast/toast';
import type {EntitlementKey, EntitlementValue} from '@models/billing';

/** The entitlements shown as the plan's allowances, in the order the catalogue lists them. */
const ENTITLEMENT_ORDER: readonly EntitlementKey[] = [
  'MAX_PRODUCTS',
  'MAX_ORDERS_MONTH',
  'MAX_ACCOUNTS',
  'MAX_STORAGE_MB',
  'CUSTOM_DOMAIN',
  'ANALYTICS',
  'PRIORITY_SUPPORT',
];

export interface AllowanceRow {
  readonly key: EntitlementKey;
  readonly label: string;
  readonly value: string;
  /** A capability the plan does not include, drawn as withheld rather than as a number. */
  readonly withheld: boolean;
}

/**
 * The billing page's own state: what it shows, and the four things it can do.
 *
 * Reads through `SubscriptionFacade` rather than fetching again, so the page and the banner are the same
 * two calls. Every action refreshes that facade on success, which is what makes the banner update in
 * place when a subscription is resumed from here.
 */
@Injectable()
export class BillingPageFacade {
  private readonly api = inject(BillingApi);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly transloco = inject(TranslocoService);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  readonly billing = inject(SubscriptionFacade);

  readonly plansOpen = signal(false);

  /** Set while any subscription-changing call is in flight; every control reads it. */
  readonly busy = signal(false);

  readonly subscription = this.billing.subscription;
  readonly invoices = this.billing.invoices;

  /**
   * What the current plan grants.
   *
   * Read from the **subscription's** entitlements rather than the catalogue's, because they can
   * legitimately differ: a grandfathered store keeps what it was sold. Within a plan, an omitted key
   * means unlimited — the catalogue's rule — so a missing entry is the most generous answer.
   *
   * **That rule only holds once there is a plan.** A subscription with no `planCode` carries an empty
   * entitlement map, and reading it through the same rule rendered every row as "Unlimited" — telling
   * a merchant whose store billing has not finished setting up that they had unlimited everything.
   * No plan means nothing to report, which the panel says instead.
   */
  readonly allowances = computed<readonly AllowanceRow[]>(() => {
    this.transloco.activeLang();
    const subscription = this.subscription();
    if (!subscription?.planCode) {
      return [];
    }
    return ENTITLEMENT_ORDER.map((key) => this.allowanceRow(key, subscription.entitlements[key]));
  });

  /** Whether renewal can be switched back on — either cancelled, or with a downgrade pending. */
  readonly canResume = computed(() => {
    const subscription = this.subscription();
    return !!subscription && (subscription.cancelAtPeriodEnd || subscription.pendingPlanChange !== null);
  });

  /**
   * Whether there is a live subscription to cancel.
   *
   * A plan is part of the question, not just a row: a `PENDING` subscription with no plan attached has
   * no renewal to switch off, and offering "Cancel renewal" against it invites an operator to cancel
   * something that has not started. A subscription already ending cannot be cancelled twice either.
   */
  readonly canCancel = computed(() => {
    const subscription = this.subscription();
    return (
      !!subscription &&
      !!subscription.planCode &&
      !subscription.cancelAtPeriodEnd &&
      subscription.status !== 'CANCELED'
    );
  });

  /**
   * Whether choosing a plan should open a hosted checkout rather than change the plan directly.
   *
   * A store with no payment provider customer has nothing to charge, so its first paid plan has to go
   * through checkout to collect a card. After that, `changePlan` is the right call — it prorates
   * against the card already on file instead of asking for it again.
   */
  private readonly needsCheckout = computed(() => !this.subscription()?.providerLinked);

  /**
   * Applies a plan the operator picked.
   *
   * The two paths end differently on purpose: checkout **leaves the console** for the provider's page,
   * so nothing after the navigation runs; `changePlan` stays here and refreshes in place.
   */
  choosePlan(planPriceId: string): void {
    const store = this.billing.storeId();
    if (!store || this.busy()) {
      return;
    }
    this.plansOpen.set(false);
    this.busy.set(true);

    if (this.needsCheckout()) {
      this.api
        .checkout(store, planPriceId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (session) => {
            this.busy.set(false);
            // A full navigation, not the router: this URL belongs to the payment provider.
            window.location.assign(session.url);
          },
          error: (error: unknown) => this.fail(error),
        });
      return;
    }

    this.api
      .changePlan(store, planPriceId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (subscription) => {
          this.busy.set(false);
          this.billing.refresh();
          // A downgrade does not take effect now, and saying "plan changed" would be untrue.
          this.toast.success(
            this.transloco.translate(
              subscription.pendingPlanChange
                ? 'billing.toast.downgradeScheduled'
                : 'billing.toast.planChanged',
              {plan: this.billing.planLabel(subscription.planCode, subscription.planDisplayName)},
            ),
          );
        },
        error: (error: unknown) => this.fail(error),
      });
  }

  /** Switches renewal back on, and calls off a scheduled downgrade if one is pending. */
  resume(): void {
    this.act((store) => this.api.resume(store), 'billing.toast.resumed');
  }

  /**
   * Switches renewal off. Never `immediate` — that is a super-admin operation and would end a period
   * the merchant has already paid for.
   */
  cancel(): void {
    this.act((store) => this.api.cancelAtPeriodEnd(store), 'billing.toast.cancelled');
  }

  private act(
    call: (store: string) => import('rxjs').Observable<unknown>,
    successKey: string,
  ): void {
    const store = this.billing.storeId();
    if (!store || this.busy()) {
      return;
    }
    this.busy.set(true);
    call(store)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.busy.set(false);
          this.billing.refresh();
          this.toast.success(this.transloco.translate(successKey));
        },
        error: (error: unknown) => this.fail(error),
      });
  }

  /**
   * Every failure here is field-less — there is no form to bind to — so it is reported rather than
   * bound. `ApiErrorService` already translates a known code; an unknown one shows the server's detail.
   */
  private fail(error: unknown): void {
    this.busy.set(false);
    this.apiErrors.notify(error);
  }

  /**
   * One entitlement, as the allowances panel reads it.
   *
   * The labels and the three allowance words live in `shared.*` rather than under `billing.*` or
   * `marketing.*`: three surfaces render this same catalogue — the pricing page, this panel, and the
   * platform's plan reference — and this facade was already reaching into the *marketing* feature's
   * namespace for its labels, which is the drift the shared namespace exists to stop.
   */
  private allowanceRow(key: EntitlementKey, value: EntitlementValue | undefined): AllowanceRow {
    const label = this.transloco.translate(`shared.entitlement.${key}.granted`);

    // Absent means unlimited — the catalogue's rule, and the opposite of "not granted".
    if (value === undefined) {
      return {key, label, value: this.transloco.translate('shared.allowance.unlimited'), withheld: false};
    }
    if (value.flagValue !== null) {
      return {
        key,
        label,
        value: this.transloco.translate(value.flagValue ? 'shared.allowance.included' : 'shared.allowance.notIncluded'),
        withheld: !value.flagValue,
      };
    }
    if (value.limitValue === null) {
      return {key, label, value: this.transloco.translate('shared.allowance.unlimited'), withheld: false};
    }
    return {
      key,
      label,
      // Through the facade so a ceiling is written in the reader's numerals, like every other figure.
      value: this.billing.formatNumber(value.limitValue),
      withheld: value.limitValue === 0,
    };
  }
}
