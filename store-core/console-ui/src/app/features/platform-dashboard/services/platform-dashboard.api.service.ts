import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map} from 'rxjs';

import {StatisticService} from '@api/analytics/statistic.service';
import type {TrendPoint} from '@models/platform';
import type {StatisticList, StatisticRange} from '@models/statistics';

/** A range with both ends chosen. The picker's own value allows either end to be null. */
export interface CompleteRange {
  readonly from: Date;
  readonly to: Date;
}

/** The two series the platform can actually count, plus their totals over the range. */
export interface PlatformSnapshot {
  readonly organizations: readonly TrendPoint[];
  readonly stores: readonly TrendPoint[];
  readonly organizationsTotal: number;
  readonly storesTotal: number;
}

/**
 * The platform's own numbers.
 *
 * **Two requests, and there is no third.** seller-ui's admin home drew three charts;
 * `subscription-statistic` — the third — exists in **no Java file on the platform**, so that panel
 * has been a 404 since it was written. It is absent here rather than empty: an empty card is a claim
 * that there is nothing to show. See lessons.md, "Platform — no subscription statistics".
 *
 * **And there is no money on it.** Nothing on the platform aggregates revenue, GMV or subscription
 * value; payments has no aggregate endpoint of any kind. So the KPI row totals organizations and
 * stores and stops. See lessons.md, "Platform — no revenue or GMV figure anywhere".
 *
 * Both legs are required — they *are* the page — so neither is wrapped in `optionalOne`. A tenancy
 * outage makes this an error page with a retry, which is the honest outcome for a page that is two
 * counts.
 */
@Injectable({providedIn: 'root'})
export class PlatformDashboardApi {
  private readonly statistics = inject(StatisticService);

  loadSnapshot(range: CompleteRange): Observable<PlatformSnapshot> {
    const window = toRange(range);
    return forkJoin({
      organizations: this.statistics.orgStatistic(window),
      stores: this.statistics.storeStatistic(window),
    }).pipe(
      map(({organizations, stores}) => {
        const orgPoints = toDailySeries(organizations, range);
        const storePoints = toDailySeries(stores, range);
        return {
          organizations: orgPoints,
          stores: storePoints,
          organizationsTotal: total(orgPoints),
          storesTotal: total(storePoints),
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
