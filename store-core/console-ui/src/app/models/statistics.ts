/**
 * Ported from seller-ui/projects/seller-core/analytics/src/lib/services/statistic.api.service.ts
 * (the inline types).
 *
 * One shape serves every statistic on the platform: `commons/domain/StatisticEntry`, a record of
 * `(date, name, value)`. What each column *means* depends entirely on the query behind the endpoint,
 * which is why the service that returns them documents each one.
 */

export interface StatisticEntry {
  /** An ISO day (`2026-08-04`) where the query grouped by day, and null where it did not. */
  readonly date: string | null;
  /** The grouping key — an order status, a country code, a SKU. Never translated by the server. */
  readonly name: string;
  readonly value: number;
}

export interface StatisticList {
  readonly entries: readonly StatisticEntry[];
}

/**
 * Mirrors `commons/domain/StatisticRange`, whose fields are `ZonedDateTime`.
 *
 * No `store`: seller-core's version carried one because its caller passed it explicitly, but the
 * request context stamps `?store=&pod=` on every request now, and a second copy in the body would be
 * a second answer to the same question.
 */
export interface StatisticRange {
  readonly fromDate: string;
  readonly toDate: string;
}

export const EMPTY_STATISTIC_LIST: StatisticList = {entries: []};
