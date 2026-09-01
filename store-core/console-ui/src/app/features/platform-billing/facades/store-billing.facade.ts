import {Injectable, computed, inject, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import type {Invoice, PlanView, SubscriptionStatus} from '@models/billing';
import {SUBSCRIPTION_STATUS_TONE, type AuditRow} from '@models/platform-billing';
import type {Tone} from '@cvhome-saas/ui-kit';
import {Money} from '@shared/i18n/money';
import {PlatformLabel} from '@shared/i18n/platform-label';
import type {SelectOption} from '@shared/ui/select/select';
import {ToastService} from '@shared/ui/toast/toast';
import {PlatformBillingApi} from '../services/platform-billing.api.service';

const MINOR_UNITS = 100;

/** Which confirmation the panel has open, if any. */
export type BillingPrompt = 'cancelAtPeriodEnd' | 'cancelNow' | 'resume' | null;

/**
 * One store's billing, for an operator who does not own it.
 *
 * **This screen was impossible until the billing guard was widened.** `subscription/current` and
 * `invoice/list` resolve to `PermissionAccessChecker.hasReadAccessOnStore`, which admitted an org
 * admin, a store admin, a store moderator and a service principal and had no super-admin branch — so
 * a platform operator was refused for every store on the platform, and "this merchant says they
 * paid" could only be answered by reading the database.
 *
 * **Every lever is gated on `providerLinked`, not on status.**
 * `SubscriptionServiceImpl.cancel` and `resume` both call `requireProviderSubscription`, so a trial
 * we granted ourselves — which has no Stripe subscription behind it — cannot be cancelled or resumed
 * at all, whatever its status says. Offering a button certain to throw is the failure this avoids.
 */
@Injectable()
export class StoreBillingFacade {
  private readonly api = inject(PlatformBillingApi);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly labels = inject(PlatformLabel);
  private readonly money = inject(Money);

  /** The store being read, set by the panel from its input. */
  readonly store = signal<string | null>(null);

  /** One write in flight. Every lever shares it: two at once on one subscription is never wanted. */
  readonly busy = signal(false);

  readonly prompt = signal<BillingPrompt>(null);

  /** Whether the plan picker is open, and which price is chosen in it. */
  readonly changingPlan = signal(false);
  readonly draftPriceId = signal('');

  private readonly loaded = snapshot(
    () => this.store() ?? undefined,
    (store) => this.api.loadStoreBilling(store),
  );

  readonly isLoading = this.loaded.isLoading;
  readonly error = this.loaded.error;
  readonly isEmpty = this.loaded.isEmpty;
  readonly reload = () => this.loaded.reload();

  readonly subscription = computed(() => this.loaded.value()?.subscription ?? null);
  readonly invoices = computed<readonly Invoice[]>(() => this.loaded.value()?.invoices ?? []);
  readonly activity = computed<readonly AuditRow[]>(() => this.loaded.value()?.activity ?? []);

  /**
   * Whether billing has a subscription for this store at all.
   *
   * `subscription/current` answers 404 for a store billing has never seen — a provisioning event
   * that never landed, or a store created before billing existed — and the leg is optional, so a
   * null arrives for that *and* for a billing that could not be reached. The panel says "no
   * subscription" for both rather than inventing a state, and the retry is what distinguishes them.
   */
  readonly hasSubscription = computed(() => !!this.subscription());

  readonly statusLabel = computed(() => this.labels.subscriptionStatus(this.subscription()?.status ?? null));

  readonly statusTone = computed<Tone>(() => {
    const status = this.subscription()?.status;
    return (status && SUBSCRIPTION_STATUS_TONE[status as SubscriptionStatus]) || 'slate';
  });

  /**
   * Whether the three levers can act at all.
   *
   * A store with no provider subscription behind it has never actually bought anything: it is on a
   * trial we granted, or it is unpaid. Stripe has nothing to move, switch off or end, and the
   * service reports that as an illegal transition rather than as a missing subscription.
   */
  readonly canAct = computed(() => this.subscription()?.providerLinked === true);

  /** Resuming is only meaningful when something is scheduled to happen. */
  readonly canResume = computed(() => {
    const subscription = this.subscription();
    return this.canAct() && (!!subscription?.cancelAtPeriodEnd || !!subscription?.pendingPlanChange);
  });

  /** Cancelling is only meaningful while renewal is still on. */
  readonly canCancel = computed(() => this.canAct() && this.subscription()?.cancelAtPeriodEnd === false);

  /**
   * The plan picker's options: every purchasable price in the catalogue, at both intervals.
   *
   * Both intervals rather than one, because a same-plan interval switch is a real operation — billing
   * counts monthly-to-yearly as an upgrade and yearly-to-monthly as a downgrade, by price.
   *
   * The price the store is already on is excluded: choosing it is a no-op the server answers by
   * returning the subscription unchanged, which reads as a lever that did nothing.
   */
  readonly planOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    const current = this.subscription()?.planPriceId;
    const plans = this.loaded.value()?.plans ?? [];
    return [...plans]
      .sort((a, b) => a.tier - b.tier)
      .flatMap((plan) => plan.prices.map((price) => ({plan, price})))
      .filter(({price}) => price.id.id !== current)
      .map(({plan, price}) => ({
        value: price.id.id,
        label: this.priceLabel(plan, price.interval, price.amount.minorUnits, price.amount.currency.code),
      }));
  });

  amount(minorUnits: number | null | undefined, currency: string | null | undefined): string {
    if (minorUnits === null || minorUnits === undefined) {
      return '—';
    }
    return this.money.account(minorUnits / MINOR_UNITS, currency);
  }

  date(value: string | null | undefined): string {
    if (!value) {
      return '—';
    }
    return this.localeFormat.localizeDate(value, undefined, {dateStyle: 'medium'});
  }

  dateTime(value: string | null | undefined): string {
    if (!value) {
      return '—';
    }
    return this.localeFormat.localizeDate(value, undefined, {dateStyle: 'medium', timeStyle: 'short'});
  }

  eventLabel(event: string | null): string {
    return this.labels.auditEvent(event);
  }

  invoiceStatusLabel(status: string | null): string {
    return this.labels.invoiceStatus(status);
  }

  actorLabel(row: AuditRow): string {
    this.transloco.activeLang();
    return row.actor || this.transloco.translate('platform.billing.activity.actorUnknown');
  }

  /* --------------------------------------------------------------------------- levers ---- */

  /**
   * Moves the store to the chosen price.
   *
   * The direction is billing's to decide, so the toast says what the server reported rather than
   * what the operator picked: an upgrade comes back on the new plan, a downgrade comes back with the
   * old plan still in force and a `pendingPlanChange` set.
   */
  changePlan(onChanged: () => void): void {
    const store = this.store();
    const priceId = this.draftPriceId();
    if (!store || !priceId || this.busy()) {
      return;
    }
    this.busy.set(true);
    this.api.changePlan(store, priceId).subscribe({
      next: (subscription) => {
        this.busy.set(false);
        this.changingPlan.set(false);
        this.draftPriceId.set('');
        this.toast.success(
          subscription.pendingPlanChange
            ? this.transloco.translate('platform.billing.store.toast.downgradeScheduled', {
                date: this.date(subscription.pendingPlanChange.effectiveAt),
              })
            : this.transloco.translate('platform.billing.store.toast.planChanged'),
        );
        this.settle(onChanged);
      },
      error: (failure: unknown) => this.fail(failure),
    });
  }

  /**
   * Cancels, at the period end or immediately.
   *
   * **Immediate is the operator's branch alone.** It throws away time the customer has already paid
   * for, and `SubscriptionService.cancel` refuses it for anyone who is not a super admin — the
   * merchant billing page does not offer it and never sends it. The dialog makes the two visibly
   * different rather than hiding one behind a checkbox.
   */
  cancel(immediate: boolean, onChanged: () => void): void {
    const store = this.store();
    if (!store || this.busy()) {
      return;
    }
    this.busy.set(true);
    this.prompt.set(null);
    this.api.cancel(store, immediate).subscribe({
      next: () => {
        this.busy.set(false);
        this.toast.success(
          this.transloco.translate(
            immediate
              ? 'platform.billing.store.toast.canceledNow'
              : 'platform.billing.store.toast.cancelScheduled',
          ),
        );
        this.settle(onChanged);
      },
      error: (failure: unknown) => this.fail(failure),
    });
  }

  resume(onChanged: () => void): void {
    const store = this.store();
    if (!store || this.busy()) {
      return;
    }
    this.busy.set(true);
    this.prompt.set(null);
    this.api.resume(store).subscribe({
      next: () => {
        this.busy.set(false);
        this.toast.success(this.transloco.translate('platform.billing.store.toast.resumed'));
        this.settle(onChanged);
      },
      error: (failure: unknown) => this.fail(failure),
    });
  }

  /**
   * Re-reads rather than echoing the response.
   *
   * The write answers a `SubscriptionView`, and using it would show what this request returned
   * rather than what billing now holds — which differs whenever the provider's webhook lands first
   * and moves the row again. Re-reading also pulls in the `subscription_audit` row the act just
   * wrote, which is the point of the Activity list beside it.
   */
  private settle(onChanged: () => void): void {
    this.loaded.reload();
    onChanged();
  }

  private fail(failure: unknown): void {
    this.busy.set(false);
    this.prompt.set(null);
    /*
     * Every refusal billing can answer here is a typed 4xx with a message — an illegal transition, a
     * provider that refused the card, a provider that could not be reached. The toast carries it
     * verbatim rather than the console guessing which of the three it was.
     */
    this.toast.danger(this.apiErrors.messageFor(failure));
  }

  private priceLabel(plan: PlanView, interval: string, minorUnits: number, currency: string): string {
    this.transloco.activeLang();
    return this.transloco.translate('platform.billing.store.priceOption', {
      plan: plan.displayName,
      amount: this.money.account(minorUnits / MINOR_UNITS, currency),
      interval: this.transloco.translate(
        interval === 'YEAR' ? 'platform.billing.store.perYear' : 'platform.billing.store.perMonth',
      ),
    });
  }
}
