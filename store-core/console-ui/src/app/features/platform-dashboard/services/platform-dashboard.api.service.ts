import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map} from 'rxjs';

import {StatisticService} from '@api/analytics/statistic.service';
import {optionalOne} from '@cvhome-saas/ui-kit';
import type {TrendPoint} from '@models/platform';
import type {StatisticList, StatisticRange} from '@models/statistics';

/** A range with both ends chosen. The picker's own value allows either end to be null. */
export interface CompleteRange {
  readonly from: Date;
  readonly to: Date;
}

/** One stacked series of the subscription chart: a plan code and its daily count. */
export interface PlanSeries {
  readonly plan: string;
  readonly points: readonly TrendPoint[];
  readonly total: number;
}

/** A period's take in one currency, in minor units. Never summed with another currency's. */
export interface CurrencyTotal {
  readonly currency: string;
  readonly minorUnits: number;
}

/** What the platform can count and, now, what it earned. */
export interface PlatformSnapshot {
  readonly organizations: readonly TrendPoint[];
  readonly stores: readonly TrendPoint[];
  readonly organizationsTotal: number;
  readonly storesTotal: number;
  /**
   * Subscriptions started per day, by plan.
   *
   * Empty rather than absent when billing could not answer: the leg is optional, and losing it
   * should cost this chart rather than the two tenant counts beside it.
   */
  readonly subscriptions: readonly PlanSeries[];
  readonly subscriptionsTotal: number;
  /** The period's revenue, one figure per currency. Empty when billing could not answer. */
  readonly revenue: readonly CurrencyTotal[];
}

/**
 * The platform's own numbers.
 *
 * **Four requests now, where there were two.** seller-ui's admin home drew three charts and the
 * third — `subscription-statistic` — existed in no Java file on the platform, so it had been a 404
 * for its entire life. It exists now, along with a revenue figure the platform previously had
 * nowhere to ask for, so the third chart is drawn and the KPI row has money on it.
 *
 * **The two tenant legs are the page and the two billing legs are not.** Organizations and stores
 * are unwrapped: losing either makes this an error page with a retry, which is honest for a page
 * that is fundamentally those two counts. The billing legs are wrapped in `optionalOne`, because a
 * billing outage should cost a chart and two tiles rather than blanking the tenant curves — and
 * because billing is the newer dependency of the two.
 *
 * The deeper reading of the money — per plan, per currency, per invoice — is `/platform/billing`.
 * This page stays a summary and links there.
 */
@Injectable({providedIn: 'root'})
export class PlatformDashboardApi {
  private readonly statistics = inject(StatisticService);

  loadSnapshot(range: CompleteRange): Observable<PlatformSnapshot> {
    const window = toRange(range);
    return forkJoin({
      organizations: this.statistics.orgStatistic(window),
      stores: this.statistics.storeStatistic(window),
      // Wrapped: this page is the tenant counts, and a billing outage should not blank them.
      subscriptions: this.statistics.subscriptionStatistic(window).pipe(optionalOne()),
      revenue: this.statistics.revenueStatistic(window).pipe(optionalOne()),
    }).pipe(
      map(({organizations, stores, subscriptions, revenue}) => {
        const orgPoints = toDailySeries(organizations, range);
        const storePoints = toDailySeries(stores, range);
        const planSeries = subscriptions ? toSeriesByName(subscriptions, range) : [];
        return {
          organizations: orgPoints,
          stores: storePoints,
          organizationsTotal: total(orgPoints),
          storesTotal: total(storePoints),
          subscriptions: planSeries.map((series) => ({
            plan: series.name,
            points: series.points,
            total: total(series.points),
          })),
          subscriptionsTotal: planSeries.reduce((sum, series) => sum + total(series.points), 0),
          revenue: revenue
            ? toSeriesByName(revenue, range).map((series) => ({
                currency: series.name,
                minorUnits: total(series.points),
              }))
            : [],
        };
      }),
    );
  }
}

/* --------------------------------------------------------------------------- shaping ---- */

const DAY_MS = 24 * 60 * 60 * 1000;

/**
 * The response, filled out to one point per day in the range.
 *
 * The queries `group by date(created_date)`, so a day on which nothing happened is **absent from the
 * response** rather than present as a zero. Plotting the rows as they arrive would draw a line that
 * skips those days and compresses the gap, which reads as a flat stretch of activity where there was
 * none. Filling the range is what makes the x-axis a calendar.
 *
 * `name` is null on every entry — the query selects the date and the count and nothing else — so
 * only `date` and `value` are read.
 */
function toDailySeries(list: StatisticList, range: CompleteRange): readonly TrendPoint[] {
  const counted = new Map<string, number>();
  for (const entry of list?.entries ?? []) {
    const day = isoDay(entry.date);
    if (day) {
      counted.set(day, (counted.get(day) ?? 0) + (entry.value ?? 0));
    }
  }

  const points: TrendPoint[] = [];
  const end = startOfDay(range.to);
  for (let day = startOfDay(range.from); day <= end; day = new Date(day.getTime() + DAY_MS)) {
    const key = isoDay(day.toISOString())!;
    points.push({date: key, value: counted.get(key) ?? 0});
  }
  return points;
}

/**
 * The same, but keeping `name` rather than summing it away — one filled-out series per key.
 *
 * {@link toDailySeries} folds every entry of a day together, which is right for the two tenancy
 * counters whose `name` is always null and wrong for both of billing's: the revenue entries are
 * keyed by currency, and summing those would invent an exchange rate, while the subscription
 * entries are keyed by plan and summing them loses the stack the chart is for.
 */
function toSeriesByName(
  list: StatisticList,
  range: CompleteRange,
): readonly {name: string; points: readonly TrendPoint[]}[] {
  const byName = new Map<string, StatisticList>();
  for (const entry of list?.entries ?? []) {
    // A null name would be a query grouping by nothing; neither of these does, but an entry that
    // lost its key should still be visible rather than dropped.
    const name = entry.name ?? '—';
    const existing = byName.get(name);
    byName.set(name, {entries: [...(existing?.entries ?? []), entry]});
  }
  return [...byName.entries()]
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([name, entries]) => ({name, points: toDailySeries(entries, range)}));
}

/**
 * The day part of whatever the server sent.
 *
 * `date(created_date)` maps to a `java.sql.Date` and serializes as `2026-08-04`, but the column is an
 * `Instant` and a driver that hands back a timestamp would serialize the time too. Taking the first
 * ten characters is right for both and wrong for neither.
 */
function isoDay(value: string | null): string | null {
  return value ? value.slice(0, 10) : null;
}

function startOfDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function total(points: readonly TrendPoint[]): number {
  return points.reduce((sum, point) => sum + point.value, 0);
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
