import {Injectable, computed, inject, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';
import {EMPTY, catchError, of} from 'rxjs';

import {SubscriptionService} from '@api/billing/subscription.service';
import {ApiError} from '@core/errors/api-error';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {PlanLabel} from '@shared/billing/plan-label';
import type {Invoice, Subscription, SubscriptionStatus} from '@models/billing';

/** How close to the end of a trial the banner starts counting down rather than staying quiet. */
const TRIAL_NOTICE_DAYS = 30;

/** The statuses that mean somebody has to do something. Everything else is left alone. */
const NEEDS_ATTENTION: readonly SubscriptionStatus[] = ['PAST_DUE', 'SUSPENDED', 'CANCELED'];

export type BannerTone = 'info' | 'warn' | 'danger';

export interface BannerView {
  readonly tone: BannerTone;
  readonly message: string;
  /** Whether the notice offers a way to pay. False for a state money cannot fix. */
  readonly canUpgrade: boolean;
}

/**
 * One store's subscription, shared by the shell's plan banner and the billing page.
 *
 * **Named for what it holds, and filed with what it serves.** It used to be `BillingFacade` under a
 * top-level `layouts/billing/`, one letter away from `features/billing`'s `BillingPageFacade` and
 * in a tier it had no business defining a feature in. The banner is shell chrome, so the state
 * behind the banner belongs to the shell; the page reads the same instance rather than fetching
 * again, which is what keeps the two from disagreeing.
 *
 * **Why this exists.** The banner used to be a hardcoded line — "You're on the Free plan" — with a dead
 * Upgrade button, shown to everyone regardless of what they were paying. `GET subscription/current?store=`
 * has always known the truth. Reading it here rather than in the banner means the page and the banner
 * cannot disagree, and a change made on the page (resume, change plan) refreshes the banner for free.
 *
 * Scoped to the store the console currently has open, and re-read when that changes: a subscription
 * belongs to a store, and an org can run a paid store beside a free one.
 */
@Injectable({providedIn: 'root'})
export class SubscriptionFacade {
  private readonly subscriptions = inject(SubscriptionService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly transloco = inject(TranslocoService);
  private readonly localeService = inject(TranslocoLocaleService);
  private readonly planLabels = inject(PlanLabel);

  /**
   * The store every call below is scoped to.
   *
   * Null before the store list has loaded, on first run, and **for a platform operator** — who has no
   * store of their own to be billed for. That last case is not cosmetic: `currentStoreId()` resolves
   * for them to whichever *tenant's* store sorts first, so without this the shell fired
   * `GET subscription/current?store=<a stranger's shop>` on every platform page and got a 403 for it.
   * See lessons.md, "Shell — a super admin's store rail is the whole platform, truncated".
   */
  readonly storeId = computed(() => (this.shell.isPlatformOperator ? null : this.shell.currentStoreId()));

  /**
   * The subscription, or `null` for a store billing has never seen.
   *
   * A 404 is **not** an error here. Billing learns about a store from `StoreCreatedEvent` through the
   * outbox, so a store created seconds ago can reach the console before its subscription row exists; so
   * can a store from before billing was introduced. Both are "no subscription yet", which the banner and
   * the page render as a state rather than reporting as a failure. Any other status still surfaces.
   */
  private readonly resource = rxResource({
    params: () => {
      const store = this.storeId();
      return store ? {store} : undefined;
    },
    stream: ({params}) =>
      this.subscriptions.current(params.store).pipe(
        catchError((error: unknown) => {
          if (error instanceof ApiError && error.status === 404) {
            return of(null);
          }
          throw error;
        }),
      ),
  });

  readonly subscription = computed<Subscription | null>(() => this.resource.value() ?? null);
  readonly isLoading = this.resource.isLoading;
  readonly error = computed(() => this.resource.error() as Error | undefined);

  /** True once a read has settled, whatever it found. The banner stays silent until then. */
  readonly settled = computed(() => !this.isLoading() && this.resource.hasValue());

  readonly status = computed<SubscriptionStatus | null>(() => this.subscription()?.status ?? null);

  /** Whether the operator is on a paid plan that is in good standing. */
  readonly healthy = computed(() => this.status() === 'ACTIVE' && !this.subscription()?.cancelAtPeriodEnd);

  /** Days left on the trial, or null when there is no trial running. */
  readonly trialDaysLeft = computed(() => daysUntil(this.subscription()?.trialEnd ?? null));

  /** Days left before a past-due store stops working. Null when nothing is overdue. */
  readonly graceDaysLeft = computed(() => daysUntil(this.subscription()?.graceUntil ?? null));

  /**
   * The banner, or null for a store that has nothing to say.
   *
   * **Null is the common case, and that is the point.** A store on a healthy paid plan gets no banner at
   * all — the old one shouted "upgrade" at paying customers. A notice appears only for a trial that is
   * running out, a payment that has failed, a cancellation already scheduled, or a store billing has
   * never seen.
   */
  readonly banner = computed<BannerView | null>(() => {
    this.transloco.activeLang();
    if (!this.settled() || !this.storeId()) {
      return null;
    }

    const subscription = this.subscription();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(key, params);

    // Billing has not caught up with this store yet. Not an upgrade prompt — there is nothing to buy
    // against a subscription that does not exist.
    if (!subscription) {
      return {tone: 'info', message: t('billing.banner.noSubscription'), canUpgrade: false};
    }

    if (NEEDS_ATTENTION.includes(subscription.status)) {
      const grace = this.graceDaysLeft();
      return {
        tone: subscription.status === 'PAST_DUE' ? 'warn' : 'danger',
        message:
          subscription.status === 'PAST_DUE'
            ? grace === null
              ? t('billing.banner.pastDue')
              : t('billing.banner.pastDueGrace', {days: this.formatNumber(Math.max(grace, 0))})
            : t(`billing.banner.${subscription.status.toLowerCase()}`),
        canUpgrade: true,
      };
    }

    // Renewal is already off. Saying "upgrade" here is wrong — the fix is to turn renewal back on.
    if (subscription.cancelAtPeriodEnd) {
      return {
        tone: 'warn',
        message: t('billing.banner.cancelling', {date: this.formatDate(subscription.currentPeriodEnd)}),
        canUpgrade: false,
      };
    }

    const trialDays = this.trialDaysLeft();
    if (subscription.status === 'TRIALING' && trialDays !== null && trialDays <= TRIAL_NOTICE_DAYS) {
      return {
        tone: trialDays <= 3 ? 'warn' : 'info',
        message: t('billing.banner.trial', {
          days: this.formatNumber(Math.max(trialDays, 0)),
          plan: this.planName(),
        }),
        canUpgrade: true,
      };
    }

    return null;
  });

  readonly planName = computed(() =>
    this.planLabel(this.subscription()?.planCode, this.subscription()?.planDisplayName),
  );

  /* ---- Invoices ------------------------------------------------------------------------------ */

  private readonly invoiceResource = rxResource({
    params: () => {
      const store = this.storeId();
      return store ? {store} : undefined;
    },
    // An invoice list nobody can read is not worth failing the billing page over: the plan panel above
    // it is the part that matters, and this degrades to empty with its own note.
    stream: ({params}) => this.subscriptions.invoices(params.store).pipe(catchError(() => EMPTY)),
  });

  readonly invoices = computed<readonly Invoice[]>(() => this.invoiceResource.value() ?? []);
  readonly invoicesLoading = this.invoiceResource.isLoading;

  /* ---- Actions -------------------------------------------------------------------------------- */

  /** Set while a subscription-changing call is in flight, so the page can disable its controls. */
  readonly working = signal(false);

  /** Re-reads the subscription and its invoices. Called after anything that changes either. */
  refresh(): void {
    this.resource.reload();
    this.invoiceResource.reload();
  }

  /**
   * A date as the reader's locale writes it, or a dash.
   *
   * Through `TranslocoLocaleService`, not `Intl.DateTimeFormat(activeLang)`. The active *language*
   * is `ar`; the console's locale for it is `ar-EG`, which is what turns `2026/09/03` into a real
   * Arabic date with Arabic-Indic numerals. Formatting off the language alone produced Latin digits
   * on an otherwise Arabic page — the same gap `Money` was written to close for amounts.
   */
  formatDate(instant: string | null): string {
    this.transloco.activeLang();
    if (!instant) {
      return '—';
    }
    const date = new Date(instant);
    if (Number.isNaN(date.getTime())) {
      return '—';
    }
    return this.localeService.localizeDate(date, undefined, {dateStyle: 'medium'});
  }

  /** A count in the reader's numerals — Arabic-Indic under `ar-EG`, grouped under `en-US`. */
  formatNumber(value: number): string {
    this.transloco.activeLang();
    return this.localeService.localizeNumber(value, 'decimal');
  }

  /** A plan's name in the reader's language. Delegated — see `PlanLabel` for why it lives there. */
  planLabel(code: string | null | undefined, displayName: string | null | undefined): string {
    return this.planLabels.of(code, displayName);
  }
}

/**
 * Whole days from now until an instant, or null.
 *
 * Rounded up, so the last partial day still reads as "1 day left" rather than "0" — a trial that says
 * zero while it is still running is a lie the operator will act on. Negative for something already past,
 * which callers clamp.
 */
function daysUntil(instant: string | null): number | null {
  if (!instant) {
    return null;
  }
  const target = new Date(instant).getTime();
  if (Number.isNaN(target)) {
    return null;
  }
  return Math.ceil((target - Date.now()) / 86_400_000);
}
