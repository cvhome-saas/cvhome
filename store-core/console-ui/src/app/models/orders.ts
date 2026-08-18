import type {PageT} from '@core/table/table.types';
import {IconName} from '@shared/ui/icon/icon-paths';
import type {KpiDatum} from '@shared/ui/kpi-card/kpi-card';
import type {Tone} from '@shared/ui/tone';

/** Where an order sits in fulfilment. The same vocabulary the dashboard charts. */
export type OrderStatus = 'Ordered' | 'Processed' | 'Delivered' | 'Refunded' | 'Canceled';

export type PaymentState = 'Paid' | 'Pending' | 'Refunded' | 'Failed';

export type OrderChannel = 'Web' | 'Phone';

/**
 * Status to its categorical tone.
 *
 * Matches `DASHBOARD_ORDER_STATUSES`, so an order is the same colour in the dashboard's
 * breakdown chart and in this table.
 */
export const STATUS_TONE: Readonly<Record<OrderStatus, Tone>> = {
  Ordered: 'green',
  Processed: 'blue',
  Delivered: 'cyan',
  Refunded: 'amber',
  Canceled: 'red',
};

/** How a payment state is drawn: never colour alone — the state is always spelled out. */
export const PAYMENT_BADGE: Readonly<Record<PaymentState, {icon: IconName; tone: Tone}>> = {
  Paid: {icon: 'checkCircle', tone: 'green'},
  Pending: {icon: 'clock', tone: 'amber'},
  Refunded: {icon: 'undo', tone: 'slate'},
  Failed: {icon: 'xCircle', tone: 'red'},
};

export const ORDER_CHANNEL_ICON: Readonly<Record<OrderChannel, IconName>> = {
  Web: 'globe',
  Phone: 'phone',
};

/** Display translation keys for the fixed vocabularies above — never the enum values themselves. */
export const ORDER_STATUS_LABEL_KEY: Readonly<Record<OrderStatus, string>> = {
  Ordered: 'orders.status.ordered',
  Processed: 'orders.status.processed',
  Delivered: 'orders.status.delivered',
  Refunded: 'orders.status.refunded',
  Canceled: 'orders.status.canceled',
};

export const PAYMENT_STATE_LABEL_KEY: Readonly<Record<PaymentState, string>> = {
  Paid: 'orders.payment.paid',
  Pending: 'orders.payment.pending',
  Refunded: 'orders.payment.refunded',
  Failed: 'orders.payment.failed',
};

export const ORDER_CHANNEL_LABEL_KEY: Readonly<Record<OrderChannel, string>> = {
  Web: 'orders.channel.web',
  Phone: 'orders.channel.phone',
};

export interface OrderRow {
  /** Display reference, including the hash: `#10482`. */
  readonly id: string;
  readonly channel: OrderChannel;
  readonly customer: string;
  readonly city: string;
  readonly status: OrderStatus;
  readonly payment: PaymentState;
  /** How it was paid: `Visa •••• 4242`, `Bank transfer`. */
  readonly paymentMeta: string;
  readonly items: number;
  readonly total: string;
  readonly placedOn: string;
  readonly placedAt: string;
  /**
   * Hours the order has gone unfulfilled. Present only on late orders — its absence is
   * what "on time" means. Formatted at render time (`orders.unfulfilledHours`) so the
   * locale's plural rules apply.
   */
  readonly unfulfilledFor?: number;
}

/** The tab strip's keys: every status, plus the unfiltered view. */
export type OrderTab = 'all' | OrderStatus;

/** A filter selection, or every channel. */
export type ChannelFilter = 'all' | OrderChannel;

/** One order KPI's source data, resolved into a `KpiDatum` by the facade. */
export interface OrderKpiSource {
  readonly labelKey: string;
  readonly value: string;
  readonly icon: KpiDatum['icon'];
  readonly tone: KpiDatum['tone'];
  readonly delta?: string;
  /** A state flag, e.g. "Late" / "All clear". Mutually exclusive with `flagCount`. */
  readonly flagKey?: string;
  /** A state flag carrying a count, e.g. "3 refunded". Mutually exclusive with `flagKey`. */
  readonly flagCount?: number;
}

/** Everything the orders page renders for one query. */
export interface OrdersSnapshot {
  readonly kpis: readonly OrderKpiSource[];
  readonly page: PageT<OrderRow>;
  /** Orders in the period before the tab and channel filters narrow it. */
  readonly totalInRange: number;
  /** Orders in the current filter that are past their fulfilment window. */
  readonly lateCount: number;
}
