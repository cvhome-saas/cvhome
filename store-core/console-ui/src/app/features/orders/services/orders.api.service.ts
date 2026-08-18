import {Injectable, inject} from '@angular/core';
import {Observable, catchError, forkJoin, map, of} from 'rxjs';

import {OrdersService, type OrderQuery} from '@api/orders/orders.service';
import {StatisticService} from '@api/analytics/statistic.service';
import type {PageRequest, PageT} from '@core/table/table.types';
import {AWAITING_FULFILMENT, formatMoney, type OrderStatus, type ReadableOrder} from '@models/checkout';
import {orderStatusLabel, type OrderKpiSource, type OrderRow, type OrdersSnapshot, type OrderTab} from '@models/orders';
import type {StatisticList} from '@models/statistics';
import type {DateRangeValue} from '@shared/ui/date-range-picker/date-range-picker';

/** What the page asks for: a filter, a search term and a page. */
export interface OrdersQuery {
  readonly tab: OrderTab;
  readonly search: string;
  readonly page: PageRequest;
  readonly range: DateRangeValue;
}

const DAY_MS = 24 * 60 * 60 * 1000;

/**
 * The orders page's data.
 *
 * Two legs, and their failure modes are deliberately different. The **list** is the page: if it
 * fails, the page failed. The **statistics** are the KPI row, and that leg is optional —
 * `order-statistic` currently 500s for every caller on a checkout bug unrelated to orders
 * (see lessons.md, "Dashboard — the three merchant statistics 500 for every caller"), and letting
 * that take the table down would mean losing the whole screen to a defect in a different endpoint.
 */
@Injectable({providedIn: 'root'})
export class OrdersApi {
  private readonly orders = inject(OrdersService);
  private readonly statistics = inject(StatisticService);

  loadSnapshot(query: OrdersQuery): Observable<OrdersSnapshot> {
    return forkJoin({
      page: this.orders.list(toOrderQuery(query)),
      counts: this.countsFor(query.range),
    }).pipe(
      map(({page, counts}) => ({
        kpis: kpis(page.totalElements, counts),
        page: {...page, content: page.content.map(toRow)},
      })),
    );
  }

  /**
   * Counts per status over the period, for the KPI row.
   *
   * One `order-statistic` call rather than one list call per status: the statistic groups by status
   * server-side, and asking the list four times to read four `totalElements` would be four round
   * trips for the same answer. Null when it fails — the tiles then say so.
   */
  private countsFor(range: DateRangeValue): Observable<Map<string, number> | null> {
    const {from, to} = range;
    if (!from || !to) {
      return of(null);
    }
    return this.statistics
      .orderStatistic({fromDate: startOfDay(from).toISOString(), toDate: endOfDay(to).toISOString()})
      .pipe(
        map(sumByStatus),
        catchError(() => of(null)),
      );
  }
}

/* --------------------------------------------------------------------------- shaping ---- */

/**
 * The page's filter, as the endpoint's parameters.
 *
 * One search box against three server fields. `OrderApi.list` matches `name`, `email` and `phone`
 * separately and ANDs whatever it is given, so the term is routed to exactly one of them by shape:
 * an `@` means an address, digits and punctuation alone mean a phone, anything else is a name.
 * Sending it as all three would return nothing at all.
 */
function toOrderQuery(query: OrdersQuery): OrderQuery {
  const term = query.search.trim();
  const field = !term ? null : term.includes('@') ? 'email' : /^[\d\s+()-]+$/.test(term) ? 'phone' : 'name';

  return {
    page: query.page.page,
    count: query.page.count,
    ...(query.tab === 'all' ? {} : {status: query.tab}),
    ...(field ? {[field]: term} : {}),
  };
}

/**
 * One order, as a table row.
 *
 * Two things the list endpoint does **not** send, both verified against the running stack:
 *
 * - `customer` is null on every row. The buyer's name and email are on `billing`, which is where the
 *   checkout wrote them, so that is what the row falls back to. The detail endpoint does populate
 *   `customer`.
 * - `products` is null too, so a line count is not available here at all — see lessons.md,
 *   "Orders — the list omits line items". The items column came off the table.
 */
function toRow(order: ReadableOrder): OrderRow {
  const person = order.customer ?? order.billing;
  const name = [person?.firstName, person?.lastName].filter(Boolean).join(' ').trim();
  const email = order.customer?.emailAddress ?? order.billing?.email ?? '';
  const address = order.delivery?.city ? order.delivery : order.billing;

  return {
    id: order.id ?? 0,
    reference: order.id === undefined ? '—' : `#${order.id}`,
    customer: name || email || '—',
    email,
    city: address?.city ?? '—',
    status: order.orderStatus ?? null,
    // The order's own payment status, humanized the same way its order status is.
    payment: order.paymentStatus ? orderStatusLabel(order.paymentStatus) : null,
    total: formatMoney(order.total, order.currency),
    placedOn: order.datePurchased ?? '',
  };
}

function sumByStatus(list: StatisticList): Map<string, number> {
  const totals = new Map<string, number>();
  for (const entry of list.entries) {
    totals.set(entry.name, (totals.get(entry.name) ?? 0) + Number(entry.value));
  }
  return totals;
}

function kpis(matching: number, counts: Map<string, number> | null): OrderKpiSource[] {
  const unavailable = counts === null;
  const awaiting = counts === null ? null : sumOf(counts, AWAITING_FULFILMENT);
  const returned = counts?.get('RETURNED') ?? null;
  const inPeriod = counts === null ? null : [...counts.values()].reduce((sum, value) => sum + value, 0);

  return [
    {
      // The one figure that comes from the list rather than the statistic, so it survives the outage.
      labelKey: 'orders.kpi.orders',
      value: String(matching),
      icon: 'shoppingCart',
      tone: 'blue',
    },
    {
      labelKey: 'orders.kpi.awaitingFulfilment',
      value: awaiting === null ? null : String(awaiting),
      icon: 'clock',
      tone: unavailable ? 'slate' : 'red',
      flagKey: unavailable
        ? 'orders.kpi.unavailable'
        : awaiting === 0
          ? 'orders.kpi.allClear'
          : 'orders.kpi.awaiting',
    },
    {
      // TODO(lessons.md): average order value needs a revenue sum, and nothing on the platform
      // provides one — see lessons.md, "Dashboard — no revenue anywhere".
      labelKey: 'orders.kpi.averageOrderValue',
      value: null,
      icon: 'chartLine',
      tone: 'slate',
      flagKey: 'orders.kpi.unavailable',
    },
    {
      labelKey: 'orders.kpi.returnsRate',
      value: rate(returned, inPeriod),
      icon: 'undo',
      tone: unavailable ? 'slate' : 'violet',
      flagKey: unavailable ? 'orders.kpi.unavailable' : undefined,
    },
  ];
}

function sumOf(counts: Map<string, number>, statuses: readonly OrderStatus[]): number {
  return statuses.reduce((sum, status) => sum + (counts.get(status) ?? 0), 0);
}

/** A percentage, or null when there is nothing to divide by — never `NaN%` and never a bare `0%`. */
function rate(part: number | null, whole: number | null): string | null {
  if (part === null || whole === null || whole === 0) {
    return null;
  }
  return `${((part / whole) * 100).toFixed(1)}%`;
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

export type {PageT};
export {DAY_MS};
