import type {IconName, Tone} from '@cvhome-saas/ui-kit';

/**
 * The dashboard speaks the shared categorical vocabulary; aliased so existing call sites
 * keep reading naturally.
 */
export type DashboardTone = Tone;

/**
 * `id` is the stable discriminator `DashboardApi` switches on to attach the right figure;
 * `labelKey` is only ever read for display. Keeping them separate means a translated label
 * can never silently break the switch that decides which KPI is which.
 */
export type KpiId = 'revenue' | 'orders' | 'pendingPayments' | 'lowStock';

export interface Kpi {
  readonly id: KpiId;
  readonly labelKey: string;
  /**
   * Null when the figure has no source — rendered as an em dash under a "Not available yet" flag,
   * never as a zero. Revenue and low stock are both null today; see lessons.md.
   */
  readonly value: string | null;
  readonly icon: IconName;
  readonly tone: DashboardTone;
  /** Movement against the preceding window of equal length. Absent when there is nothing to compare. */
  readonly delta?: string;
  readonly trend?: 'up' | 'down';
  readonly flagKey?: string;
}

/** The attention queue's source data — resolved into the shared action-list's `ActionItem` by the facade. */
export interface AttentionItem {
  readonly labelKey: string;
  readonly detailKey: string;
  /** Outstanding count shown as a chip. Null when the service that would answer it is unreachable. */
  readonly count: string | null;
  readonly icon: IconName;
  readonly tone: DashboardTone;
}

export interface OrderStatus {
  /**
   * Already resolved rather than a key: the server returns raw `OrderStatus` names and the console
   * translates only the ones it knows, humanizing the rest. A key here would mean the facade could be
   * handed one that does not exist, which the strict missing handler turns into a thrown error.
   */
  readonly label: string;
  readonly value: number;
  readonly tone: DashboardTone;
}

/**
 * A row of the top-products list.
 *
 * `sku`, not a name, and `orders`, not units or revenue — that is what `product-statistic` returns.
 * The previous `{name, sales}` shape implied both a catalog lookup and a unit that never existed.
 */
export interface Product {
  readonly sku: string;
  readonly orders: number;
}


/**
 * One slice of the orders-by-country donut.
 *
 * `label` is a country code straight from the order's billing address — data, not copy, so there is
 * nothing to translate and no key to look up.
 */
export interface CustomerSplitSegment {
  readonly label: string;
  readonly value: number;
  readonly tone: DashboardTone;
}

/** Everything the dashboard page renders for one reporting period. */
export interface DashboardSnapshot {
  readonly kpis: readonly Kpi[];
  readonly attention: readonly AttentionItem[];
  readonly orderStatuses: readonly OrderStatus[];
  readonly products: readonly Product[];
  readonly customerSplit: readonly CustomerSplitSegment[];
}
