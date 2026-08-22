import {Injectable, inject} from '@angular/core';
import {Observable, catchError, forkJoin, map, of} from 'rxjs';

import {StatisticService} from '@api/analytics/statistic.service';
import {PaymentService} from '@api/payment/payment.service';
import type {
  AttentionItem,
  CustomerSplitSegment,
  DashboardSnapshot,
  Kpi,
  OrderStatus,
  Product,
} from '@models/dashboard';
import type {StatisticEntry, StatisticList, StatisticRange} from '@models/statistics';
import type {Tone} from '@shared/ui/tone';

/**
 * A range with both ends chosen.
 *
 * The picker's `DateRangeValue` allows either end to be null, because it models a selection in
 * progress. A request cannot be made from half a range, so the narrowing happens once, at the facade,
 * and this service only ever sees a complete one.
 */
export interface CompleteRange {
  readonly from: Date;
  readonly to: Date;
}

/** How many products the ranked list shows. */
const TOP_PRODUCT_COUNT = 5;

/**
 * The order statuses that mean the seller still has something to do.
 *
 * `OrderStatus` has ten values; everything before a parcel moves is work outstanding. Deliberately a
 * list of what *is* unfulfilled rather than what is not, so a status added to the enum later is
 * treated as fulfilled — the wrong direction to guess in is the one that invents an alarm.
 */
const AWAITING_FULFILMENT: readonly string[] = ['CREATED', 'PENDING_PAYMENT', 'CONFIRMED', 'PROCESSING'];

/**
 * A stable colour per order status, so a status keeps the same slice and bar between renders.
 *
 * Grouped by meaning rather than spread across the palette: the healthy path is green through cyan,
 * work outstanding is blue and amber, and the two failure endings are red and violet.
 */
const STATUS_TONES: Readonly<Record<string, Tone>> = {
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

/** What an unmapped status falls back to, cycled so two of them are still distinguishable. */
const FALLBACK_TONES: readonly Tone[] = ['slate', 'blue', 'cyan', 'amber', 'violet'];

/**
 * The dashboard's figures, assembled from what the platform can actually answer.
 *
 * Five requests. The three statistics are required — they are the page. The payment count is
 * **optional**: a payments outage costs the two payment figures and nothing else, and reports itself
 * as unavailable rather than as zero, because "no approvals waiting" is the most dangerous wrong
 * answer this page could give.
 *
 * Everything here is a count. There is no revenue anywhere on the platform and no stock level the
 * catalog will report, so two of the four KPI tiles are permanently null — see lessons.md,
 * "Dashboard — no revenue anywhere" and "Dashboard — no stock levels".
 */
@Injectable({providedIn: 'root'})
export class DashboardApi {
  private readonly statistics = inject(StatisticService);
  private readonly payments = inject(PaymentService);

  loadSnapshot(range: CompleteRange): Observable<DashboardSnapshot> {
    const current = toRange(range);
    const previous = toRange(precedingWindow(range));

    return forkJoin({
      orders: this.statistics.orderStatistic(current),
      previousOrders: this.statistics.orderStatistic(previous),
      countries: this.statistics.customerStatistic(current),
      products: this.statistics.productStatistic(current),
      /*
       * TODO(lessons.md): counting requires fetching a page — see lessons.md, "Dashboard — counting
       * requires fetching". Null rather than 0 when payments cannot be reached.
       *
       * This counted `WAITING_VERIFICATION` when the tile shipped, and therefore counted **zero,
       * always**: no processor ever sets that status — a manual transfer awaiting a person sits in
       * `PENDING`. It was not visibly wrong, because zero is a plausible answer, which is what made
       * it worth finding. See lessons.md, "Payments — the approval queue's own status is never set".
       */
      awaitingApproval: this.payments.countAwaitingApproval().pipe(catchError(() => of(null))),
    }).pipe(
      map(({orders, previousOrders, countries, products, awaitingApproval}) => {
        const byStatus = sumByName(orders);
        const orderCount = total(byStatus);
        const previousCount = total(sumByName(previousOrders));
        const unfulfilled = sumWhere(byStatus, (status) => AWAITING_FULFILMENT.includes(status));

        return {
          kpis: kpis(orderCount, previousCount, awaitingApproval),
          attention: attention(awaitingApproval, unfulfilled),
          orderStatuses: orderStatuses(byStatus),
          products: topProducts(products),
          customerSplit: customerSplit(countries),
        } satisfies DashboardSnapshot;
      }),
    );
  }
}

/* --------------------------------------------------------------------------- shaping ---- */

/** Collapses the day dimension away: the widgets want a total per status over the whole range. */
function sumByName(list: StatisticList): Map<string, number> {
  const totals = new Map<string, number>();
  for (const entry of list.entries) {
    totals.set(entry.name, (totals.get(entry.name) ?? 0) + Number(entry.value));
  }
  return totals;
}

function total(totals: Map<string, number>): number {
  let sum = 0;
  for (const value of totals.values()) {
    sum += value;
  }
  return sum;
}

function sumWhere(totals: Map<string, number>, predicate: (name: string) => boolean): number {
  let sum = 0;
  for (const [name, value] of totals) {
    if (predicate(name)) {
      sum += value;
    }
  }
  return sum;
}

function kpis(orders: number, previousOrders: number, awaitingApproval: number | null): Kpi[] {
  const movement = delta(orders, previousOrders);

  return [
    {
      // TODO(lessons.md): revenue — nothing on the platform sums an order total. See lessons.md,
      // "Dashboard — no revenue anywhere".
      id: 'revenue',
      labelKey: 'dashboard.kpi.revenue',
      value: null,
      icon: 'dollar',
      tone: 'slate',
      flagKey: 'dashboard.kpi.unavailable',
    },
    {
      id: 'orders',
      labelKey: 'dashboard.kpi.orders',
      value: String(orders),
      icon: 'shoppingCart',
      tone: 'blue',
      ...(movement ?? {}),
    },
    {
      id: 'pendingPayments',
      labelKey: 'dashboard.kpi.pendingPayments',
      value: awaitingApproval === null ? null : String(awaitingApproval),
      icon: 'creditCard',
      tone: awaitingApproval === null ? 'slate' : 'amber',
      flagKey:
        awaitingApproval === null
          ? 'dashboard.kpi.unavailable'
          : awaitingApproval > 0
            ? 'dashboard.kpi.needsReview'
            : undefined,
    },
    {
      // TODO(lessons.md): low stock — `ProductCriteria` has no quantity field, so the question cannot
      // be asked. See lessons.md, "Dashboard — no stock levels".
      id: 'lowStock',
      labelKey: 'dashboard.kpi.lowStock',
      value: null,
      icon: 'box',
      tone: 'slate',
      flagKey: 'dashboard.kpi.unavailable',
    },
  ];
}

/**
 * Percentage movement against the preceding window.
 *
 * Omitted entirely when the previous window had no orders: every change from zero is an infinite
 * increase, and "↑ 100%" for a store's first two orders is worse than saying nothing.
 */
function delta(orders: number, previousOrders: number): Pick<Kpi, 'delta' | 'trend'> | null {
  if (previousOrders === 0) {
    return null;
  }
  const change = ((orders - previousOrders) / previousOrders) * 100;
  return {
    delta: `${Math.abs(change).toFixed(1)}%`,
    trend: change < 0 ? 'down' : 'up',
  };
}

function attention(awaitingApproval: number | null, unfulfilled: number): AttentionItem[] {
  return [
    {
      labelKey: 'dashboard.attention.paymentApprovals.label',
      detailKey: 'dashboard.attention.paymentApprovals.detail',
      count: awaitingApproval === null ? null : String(awaitingApproval),
      icon: 'creditCard',
      tone: 'amber',
    },
    {
      // Retitled from "past 24 hours without a status update": nothing reports when a status last
      // changed, only when the order was placed. See lessons.md, "Dashboard — no stale-order signal".
      labelKey: 'dashboard.attention.awaitingFulfilment.label',
      detailKey: 'dashboard.attention.awaitingFulfilment.detail',
      count: String(unfulfilled),
      icon: 'clock',
      tone: 'red',
    },
    // TODO(lessons.md): the third row was "Low stock products" and has no source — see lessons.md,
    // "Dashboard — no stock levels".
  ];
}

/** Highest first, so the bar chart reads as a ranking. */
function orderStatuses(byStatus: Map<string, number>): OrderStatus[] {
  return [...byStatus]
    .sort(([, a], [, b]) => b - a)
    .map(([status, value], index) => ({
      label: statusLabel(status),
      value,
      tone: STATUS_TONES[status] ?? FALLBACK_TONES[index % FALLBACK_TONES.length],
    }));
}

/**
 * A status name a person can read.
 *
 * Not translated. The console knows ten statuses today and the server owns that enum; a
 * `translate('dashboard.orderStatus.' + name)` on an eleventh would **throw**, because Transloco is
 * configured with a strict missing handler. Humanizing is total, and reads acceptably for anything
 * the enum grows.
 */
function statusLabel(status: string): string {
  return status
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

function topProducts(list: StatisticList): Product[] {
  return [...list.entries]
    .sort((a, b) => Number(b.value) - Number(a.value))
    .slice(0, TOP_PRODUCT_COUNT)
    .map((entry) => ({sku: entry.name, orders: Number(entry.value)}));
}

/** Countries, biggest share first. The label is the billing country code as the order recorded it. */
function customerSplit(list: StatisticList): CustomerSplitSegment[] {
  return [...list.entries]
    .sort((a, b) => Number(b.value) - Number(a.value))
    .map((entry: StatisticEntry, index) => ({
      label: entry.name,
      value: Number(entry.value),
      tone: FALLBACK_TONES[index % FALLBACK_TONES.length],
    }));
}

/* ----------------------------------------------------------------------------- range ---- */

const DAY_MS = 24 * 60 * 60 * 1000;

/**
 * The window of equal length immediately before this one, for the period-over-period delta.
 *
 * Inclusive of both ends, matching how the range picker reads: a 30-day range compares against the 30
 * days before it, not 29.
 */
function precedingWindow(range: CompleteRange): CompleteRange {
  const span = range.to.getTime() - range.from.getTime() + DAY_MS;
  return {from: new Date(range.from.getTime() - span), to: new Date(range.from.getTime() - DAY_MS)};
}

/**
 * The wire shape, with the day widened to its full extent.
 *
 * The server compares against `datePurchased`, so a `to` left at midnight would silently exclude
 * everything that happened on the last day of the range the seller picked.
 */
function toRange(range: CompleteRange): StatisticRange {
  return {
    fromDate: startOfDay(range.from).toISOString(),
    toDate: endOfDay(range.to).toISOString(),
  };
}

function startOfDay(date: Date): Date {
  const copy = new Date(date);
  copy.setHours(0, 0, 0, 0);
  return copy;
}

function endOfDay(date: Date): Date {
  const copy = new Date(date);
  copy.setHours(23, 59, 59, 999);
  return copy;
}
