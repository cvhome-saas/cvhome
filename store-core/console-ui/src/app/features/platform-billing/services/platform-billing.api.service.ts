import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map} from 'rxjs';

import {StatisticService} from '@api/analytics/statistic.service';
import {
  PlatformBillingService,
  type AuditQuery,
  type InvoiceQuery,
  type SubscriptionQuery,
} from '@api/billing/platform-billing.service';
import {SubscriptionService} from '@api/billing/subscription.service';
import {optionalOne} from '@cvhome-saas/ui-kit';
import type {Invoice, PlanView, Subscription} from '@models/billing';
import {
  toAuditRow,
  toInvoiceRow,
  toSubscriptionRow,
  type AuditRow,
  type BillingHealthDto,
  type InvoiceTotalDto,
  type PlanStatisticDto,
  type PlatformInvoiceRow,
  type PlatformSubscriptionRow,
} from '@models/platform-billing';
import type {StatisticList, StatisticRange} from '@models/statistics';

/** A range with both ends chosen. The picker's own value allows either end to be null. */
export interface CompleteRange {
  readonly from: Date;
  readonly to: Date;
}

/** One paged listing, with the filter it answers echoed back. */
export interface ListSnapshot<T> {
  readonly rows: readonly T[];
  readonly totalElements: number;
  readonly totalPages: number;
  /**
   * The term these rows answer.
   *
   * The table keeps the last good rows on screen while the next request is in flight, so "the term"
   * and "the rows" are briefly out of step — and the empty state has to know which of the two it is
   * describing, or a slow search shows "nothing matched" over the previous query's results.
   */
  readonly term: string;
}

/** A revenue figure for one currency over the chosen period. */
export interface CurrencyTotal {
  readonly currency: string;
  readonly minorUnits: number;
}

/** One point of a stacked day series. */
export interface NamedSeries {
  readonly name: string;
  readonly points: readonly {date: string; value: number}[];
  readonly total: number;
}

/** Everything the Overview tab draws. */
export interface OverviewSnapshot {
  /** Collected per day, per currency. Minor units, never converted. */
  readonly revenue: readonly NamedSeries[];
  /** The period's take, one figure per currency. */
  readonly revenueTotals: readonly CurrencyTotal[];
  readonly plans: PlanStatisticDto | null;
  readonly health: BillingHealthDto | null;
  /** The first page of the stores billing has cut off, and how many there are in total. */
  readonly blocked: readonly PlatformSubscriptionRow[];
  readonly blockedTotal: number;
}

/** One store's billing, as the detail panel reads it. */
export interface StoreBillingSnapshot {
  readonly subscription: Subscription | null;
  readonly invoices: readonly Invoice[];
  readonly activity: readonly AuditRow[];
  /** The catalogue behind the plan picker. Empty when it could not be read. */
  readonly plans: readonly PlanView[];
}

/** How many rows the store panel shows of each of its two lists. */
const PANEL_ROWS = 8;

/** How many blocked stores the Overview lists before deferring to the Subscriptions tab. */
const BLOCKED_PREVIEW_ROWS = 8;

/**
 * The platform's billing reads, in the seam every feature has.
 *
 * **Nothing here is a fixture and nothing here is computed from a different question.** Every figure
 * comes from an endpoint that sums or counts the thing it is labelled as — which is new: until this
 * change billing exposed no aggregate at all, and `/platform` said so in a notice bar.
 *
 * **Money is never converted and never summed across currencies.** Nothing on the platform holds an
 * exchange rate, so each currency stays its own figure all the way to the tile that draws it. That
 * is why {@link OverviewSnapshot.revenueTotals} is a list rather than a number.
 */
@Injectable({providedIn: 'root'})
export class PlatformBillingApi {
  private readonly platform = inject(PlatformBillingService);
  private readonly subscriptions = inject(SubscriptionService);
  private readonly statistics = inject(StatisticService);

  /**
   * The Overview tab: four legs, one of which is the page.
   *
   * The revenue series is the subject — it is what the tab is *for* — so it is unwrapped and a
   * failure reaches the error state with a retry. The other three are wrapped in `optionalOne`: a
   * plan mix that cannot be read costs a donut, and a health count that cannot be read costs two
   * numbers, whereas a failed `forkJoin` costs the whole screen.
   */
  loadOverview(range: CompleteRange): Observable<OverviewSnapshot> {
    return forkJoin({
      revenue: this.statistics.revenueStatistic(toRange(range)),
      plans: this.platform.planStatistics().pipe(optionalOne()),
      health: this.platform.health().pipe(optionalOne()),
      blocked: this.platform
        .subscriptions({blockedOnly: true}, 0, BLOCKED_PREVIEW_ROWS)
        .pipe(optionalOne()),
    }).pipe(
      map(({revenue, plans, health, blocked}) => {
        const series = toNamedSeries(revenue, range);
        return {
          revenue: series,
          revenueTotals: series.map((entry) => ({currency: entry.name, minorUnits: entry.total})),
          plans,
          health,
          blocked: (blocked?.content ?? []).map(toSubscriptionRow),
          blockedTotal: blocked?.totalElements ?? 0,
        };
      }),
    );
  }

  /** Subscriptions started per day, by plan code — the platform dashboard's third chart. */
  loadSubscriptionSeries(range: CompleteRange): Observable<readonly NamedSeries[]> {
    return this.statistics
      .subscriptionStatistic(toRange(range))
      .pipe(map((list) => toNamedSeries(list, range)));
  }

  loadSubscriptions(
    query: SubscriptionQuery,
    page: number,
    count: number,
  ): Observable<ListSnapshot<PlatformSubscriptionRow>> {
    return this.platform.subscriptions(query, page, count).pipe(
      map((page) => ({
        rows: (page.content ?? []).map(toSubscriptionRow),
        totalElements: page.totalElements,
        totalPages: page.totalPages,
        term: query.term ?? '',
      })),
    );
  }

  loadInvoices(
    query: InvoiceQuery,
    page: number,
    count: number,
  ): Observable<ListSnapshot<PlatformInvoiceRow>> {
    return this.platform.invoices(query, page, count).pipe(
      map((page) => ({
        rows: (page.content ?? []).map(toInvoiceRow),
        totalElements: page.totalElements,
        totalPages: page.totalPages,
        term: query.store ?? '',
      })),
    );
  }

  /**
   * What the filtered ledger comes to, one figure per currency.
   *
   * A separate call on the same filter, so the rows can render while the sums are computed. The page
   * treats a failure as "no totals yet" rather than as a failed ledger, which is why the caller
   * wraps it rather than this method throwing through.
   */
  loadInvoiceTotals(query: InvoiceQuery): Observable<readonly InvoiceTotalDto[]> {
    return this.platform.invoiceTotals(query);
  }

  loadAudit(query: AuditQuery, page: number, count: number): Observable<ListSnapshot<AuditRow>> {
    return this.platform.audit(query, page, count).pipe(
      map((page) => ({
        rows: (page.content ?? []).map(toAuditRow),
        totalElements: page.totalElements,
        totalPages: page.totalPages,
        term: query.store ?? '',
      })),
    );
  }

  /**
   * One store's billing: what it is on, what it was charged, and what has happened to it.
   *
   * All four legs are optional and none is the panel: a store with no subscription is a state the
   * panel renders rather than an error, and it is exactly the state an operator opens this to
   * confirm. `subscription/current` answers 404 for a store billing has never seen, which is a real
   * answer — see `SubscriptionService.current`.
   */
  loadStoreBilling(store: string): Observable<StoreBillingSnapshot> {
    return forkJoin({
      subscription: this.subscriptions.current(store).pipe(optionalOne()),
      // A store with no invoices and a billing that could not answer both render as "nothing yet";
      // the subscription panel above is what carries the failure.
      invoices: this.subscriptions.invoices(store, PANEL_ROWS).pipe(optionalOne()),
      activity: this.platform.audit({store}, 0, PANEL_ROWS).pipe(optionalOne()),
      // The catalogue behind the plan picker. Losing it costs the picker its options, not the panel.
      plans: this.subscriptions.plans().pipe(optionalOne()),
    }).pipe(
      map(({subscription, invoices, activity, plans}) => ({
        subscription,
        invoices: invoices ?? [],
        activity: (activity?.content ?? []).map(toAuditRow),
        plans: plans ?? [],
      })),
    );
  }

  /* --------------------------------------------------------------------------- levers ---- */

  /**
   * Moves a store to another plan.
   *
   * The direction is not the caller's to choose: billing decides from the tier and the price, and
   * answers with what actually happened — an upgrade comes back on the new plan, a downgrade comes
   * back with `pendingPlanChange` set and the old plan still in force.
   */
  changePlan(store: string, planPriceId: string): Observable<Subscription> {
    return this.subscriptions.changePlan(store, planPriceId);
  }

  /**
   * Cancels a subscription, at the period end or immediately.
   *
   * **`immediate` is the branch only a platform operator has.**
   * `SubscriptionService.cancel` refuses it for anyone else with `ImmediateCancelForbiddenException`,
   * and no client had ever sent it — the console's merchant billing page does not offer it at all.
   * It throws away time the customer has paid for, so the dialog distinguishes the two.
   */
  cancel(store: string, immediate: boolean): Observable<Subscription> {
    return this.subscriptions.cancel(store, immediate);
  }

  /** Switches renewal back on, and calls off a scheduled downgrade if one is pending. */
  resume(store: string): Observable<Subscription> {
    return this.subscriptions.resume(store);
  }
}

/* --------------------------------------------------------------------------- shaping ---- */

const DAY_MS = 24 * 60 * 60 * 1000;

/**
 * A `(date, name, value)` response as one filled-out day series per `name`.
 *
 * Two things it does that a naive read does not.
 *
 * **It fills the range.** Both queries `group by` a day, so a day on which nothing happened is
 * *absent* from the response rather than present as a zero. Plotting the rows as they arrive draws a
 * line that skips those days and compresses the gap, which reads as a flat stretch of activity where
 * there was none.
 *
 * **It keeps `name` rather than summing it away.** `platform-dashboard`'s `toDailySeries` folds
 * every entry of a day together, which is right for the two tenancy counters whose `name` is always
 * null and wrong for both of these: summing currencies is a wrong number, and summing plans loses
 * the stack the chart is for.
 */
function toNamedSeries(list: StatisticList, range: CompleteRange): readonly NamedSeries[] {
  const byName = new Map<string, Map<string, number>>();
  for (const entry of list?.entries ?? []) {
    const day = isoDay(entry.date);
    if (!day) {
      continue;
    }
    // A null name would be a query that groups by nothing; neither of these does, but an entry that
    // lost its key should still be visible rather than dropped.
    const name = entry.name ?? '—';
    const days = byName.get(name) ?? new Map<string, number>();
    days.set(day, (days.get(day) ?? 0) + (entry.value ?? 0));
    byName.set(name, days);
  }

  const calendar = daysOf(range);
  return [...byName.entries()]
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([name, days]) => {
      const points = calendar.map((day) => ({date: day, value: days.get(day) ?? 0}));
      return {name, points, total: points.reduce((sum, point) => sum + point.value, 0)};
    });
}

/** Every day in the range, as ISO days, so the x-axis is a calendar rather than a list of hits. */
function daysOf(range: CompleteRange): readonly string[] {
  const days: string[] = [];
  const end = startOfDay(range.to);
  for (let day = startOfDay(range.from); day <= end; day = new Date(day.getTime() + DAY_MS)) {
    days.push(isoDay(day.toISOString())!);
  }
  return days;
}

/**
 * The day part of whatever the server sent.
 *
 * The queries cast to `varchar` and serialize as `2026-08-04`, but a driver that handed back a
 * timestamp would serialize the time too. Taking the first ten characters is right for both.
 */
function isoDay(value: string | null): string | null {
  return value ? value.slice(0, 10) : null;
}

function startOfDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

/**
 * The range as `StatisticRange` wants it.
 *
 * The fields are `ZonedDateTime` on the server, so an offset is required — `toISOString()` gives a
 * `Z` one, which parses. The end is pushed to the end of its day: a range picked as "the 1st to the
 * 30th" that stopped at midnight on the 30th would silently drop that whole day, which is the most
 * recent one and the one an operator is looking at.
 */
function toRange(range: CompleteRange): StatisticRange {
  const to = new Date(range.to);
  to.setHours(23, 59, 59, 999);
  const from = new Date(range.from);
  from.setHours(0, 0, 0, 0);
  return {fromDate: from.toISOString(), toDate: to.toISOString()};
}
