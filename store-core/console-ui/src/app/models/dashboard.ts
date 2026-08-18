import {IconName} from '@shared/ui/icon/icon-paths';
import type {Tone} from '@shared/ui/tone';

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
  readonly value: string;
  readonly icon: IconName;
  readonly tone: DashboardTone;
  readonly delta?: string;
  readonly flagKey?: string;
}

/** The attention queue's source data — resolved into the shared action-list's `ActionItem` by the facade. */
export interface AttentionItem {
  readonly labelKey: string;
  readonly detailKey: string;
  /** Outstanding count shown as a chip, e.g. `7`. */
  readonly count: string;
  readonly icon: IconName;
  readonly tone: DashboardTone;
}

export interface OrderStatus {
  readonly labelKey: string;
  readonly value: number;
  readonly tone: DashboardTone;
}

/** Bar widths and ranking are derived from `sales` by the widget that renders it. */
export interface Product {
  readonly name: string;
  readonly sales: number;
}


/** Source for the customer-split donut, resolved into `DonutSlice`s by the facade. */
export interface CustomerSplitSegment {
  readonly labelKey: string;
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
