import type {PageT} from '@core/table/table.types';
import {INVENTORY_STATUSES, ORDER_STATUSES, PAYMENT_STATUSES, type OrderStatus} from '@models/checkout';
import type {KpiDatum} from '@shared/ui/kpi-card/kpi-card';
import type {Tone} from '@shared/ui/tone';

/**
 * The orders page's view models.
 *
 * The wire shapes live in `@models/checkout`. What changed when this page went from fixture to real
 * orders: the five invented statuses (`Ordered | Processed | Delivered | Refunded | Canceled`) are
 * gone, replaced by the server's ten-value enum, and with them the channel column, the card-brand
 * line and the late badge — none of which any field on an order carries. See lessons.md.
 */

/**
 * Status to its categorical tone.
 *
 * Grouped by meaning rather than spread across the palette, and **identical to the dashboard's map**
 * so an order is the same colour in the breakdown chart and in this table.
 */
export const STATUS_TONE: Readonly<Record<OrderStatus, Tone>> = {
  CREATED: 'slate',
  PENDING_PAYMENT: 'amber',
  CONFIRMED: 'blue',
  PROCESSING: 'blue',
  SHIPPED: 'cyan',
  DELIVERING: 'cyan',
  DELIVERED: 'green',
  COMPLETED: 'green',
  CANCELLED: 'red',
  RETURNED: 'violet',
};

/**
 * Every status value the console has a translation for — the three server enums an order carries.
 *
 * The set is what makes translating them safe. Transloco is configured to throw on a missing key,
 * so a status added server-side would take the page down if it were looked up blind; membership is
 * checked first and anything unrecognised is humanized instead. See `StatusLabel`.
 */
export const KNOWN_STATUSES: ReadonlySet<string> = new Set<string>([
  ...ORDER_STATUSES,
  ...PAYMENT_STATUSES,
  ...INVENTORY_STATUSES,
]);

/**
 * A status as a person reads it, in English, from the enum name alone.
 *
 * The fallback for a value the console has never seen — and the only rendering available where
 * there is no injector, such as inside a pure mapping function.
 */
export function humanizeStatus(status: string): string {
  return status
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

/** One row of the orders table. Every field is read off the order the list already returned. */
export interface OrderRow {
  /** The numeric id, used for the row's link. */
  readonly id: number;
  /** Display reference, including the hash: `#10482`. */
  readonly reference: string;
  readonly customer: string;
  readonly email: string;
  readonly city: string;
  readonly status: OrderStatus | null;
  /** The order's own `paymentStatus`, as the server's enum name. Null when it carries none. */
  readonly payment: string | null;
  /**
   * The order's total as an amount, formatted by the page rather than by the mapping — a rendered
   * string here would keep its old language after a switch. `text` is the server's own formatting
   * where it sends any; it is null on every order the running stack returns.
   *
   * There is no item count: the list endpoint sends no `products` — see lessons.md, "Orders — the
   * list omits line items".
   */
  readonly total: {readonly value: number | null; readonly currency: string | null; readonly text: string | null};
  readonly placedOn: string;
}

/** The filter strip's selection: one real status, or every order. */
export type OrderTab = 'all' | OrderStatus;

/** One order KPI's source data, resolved into a `KpiDatum` by the facade. */
export interface OrderKpiSource {
  readonly labelKey: string;
  /** Null when the figure has no source — rendered as an em dash under a flag, never as a zero. */
  readonly value: string | null;
  readonly icon: KpiDatum['icon'];
  readonly tone: KpiDatum['tone'];
  readonly delta?: string;
  readonly trend?: 'up' | 'down';
  readonly flagKey?: string;
}

/** Everything the orders page renders for one query. */
export interface OrdersSnapshot {
  readonly kpis: readonly OrderKpiSource[];
  readonly page: PageT<OrderRow>;
}
