import {Injectable} from '@angular/core';
import {Observable, delay, of, throwError} from 'rxjs';

import type {PageRequest, PageT} from '@core/table/table.types';
import {ORDERS} from '@mocks/orders.fixture';
import type {
  ChannelFilter,
  OrderKpiSource,
  OrderRow,
  OrderTab,
  OrdersSnapshot,
} from '@models/orders';
import type {DateRangeValue} from '@shared/ui/date-range-picker/date-range-picker';

const DAY_MS = 24 * 60 * 60 * 1000;

/** Round-trip the mock pretends to take, so loading states are actually exercised. */
const MIN_LATENCY_MS = 450;
const MAX_LATENCY_MS = 900;

/**
 * Fraction of requests that fail, for exercising the error path by hand. Kept at 0 so the
 * app is predictable; the error path is covered by a failing stub in the spec instead.
 */
const FAILURE_RATE = 0;

/** What the page is asking for: a slice of the book, under the current filters. */
export interface OrdersQuery {
  readonly range: DateRangeValue;
  readonly tab: OrderTab;
  readonly channel: ChannelFilter;
  readonly page: PageRequest;
}

/** The order book. Console chrome is served by `ConsoleApi`. */
@Injectable({providedIn: 'root'})
export class OrdersApi {
  /**
   * Loads one page of orders, with the headline metrics for the same period.
   *
   * Stands in for a real endpoint: it takes time, and filtering and paging happen here
   * rather than in the page, so the page is written against the same contract a server
   * would satisfy.
   */
  loadOrders(query: OrdersQuery): Observable<OrdersSnapshot> {
    const latency = MIN_LATENCY_MS + Math.random() * (MAX_LATENCY_MS - MIN_LATENCY_MS);

    if (Math.random() < FAILURE_RATE) {
      return throwError(() => new Error('Unable to load orders.')).pipe(delay(latency));
    }

    return of(this.snapshotFor(query)).pipe(delay(latency));
  }

  /**
   * Derives a snapshot from the query. Deterministic — the same query always returns the
   * same orders, so the UI is stable across reloads.
   */
  private snapshotFor(query: OrdersQuery): OrdersSnapshot {
    const days = this.spanInDays(query.range);
    // A 30-day period shows the whole book; shorter periods show proportionally less of it.
    const inRange = ORDERS.slice(0, Math.max(1, Math.round(ORDERS.length * (days / 30))));

    const matching = inRange.filter(
      (order) =>
        (query.tab === 'all' || order.status === query.tab) &&
        (query.channel === 'all' || order.channel === query.channel),
    );

    return {
      kpis: this.kpisFor(inRange, days),
      page: this.pageOf(matching, query.page),
      totalInRange: inRange.length,
      lateCount: matching.filter((order) => order.unfulfilledFor).length,
    };
  }

  /** Metrics describe the period, not the tab — they do not move when a filter narrows. */
  private kpisFor(inRange: readonly OrderRow[], days: number): readonly OrderKpiSource[] {
    const unfulfilled = inRange.filter((order) => order.unfulfilledFor).length;
    const refunded = inRange.filter((order) => order.status === 'Refunded').length;

    const revenue = inRange
      .filter((order) => order.status !== 'Canceled')
      .reduce((sum, order) => sum + this.amountOf(order.total), 0);
    const average = revenue / Math.max(1, inRange.length);
    const returnsRate = (refunded / Math.max(1, inRange.length)) * 100;

    return [
      {
        labelKey: 'orders.kpi.orders',
        value: inRange.length.toLocaleString(),
        icon: 'shoppingCart',
        tone: 'blue',
        delta: this.delta(days, 5.8),
      },
      {
        labelKey: 'orders.kpi.unfulfilled',
        value: `${unfulfilled}`,
        icon: 'clock',
        tone: 'red',
        flagKey: unfulfilled > 0 ? 'orders.kpi.late' : 'orders.kpi.allClear',
      },
      {
        labelKey: 'orders.kpi.averageOrderValue',
        value: `$${average.toLocaleString(undefined, {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        })}`,
        icon: 'chartLine',
        tone: 'green',
        delta: this.delta(days, 3.1),
      },
      {
        labelKey: 'orders.kpi.returnsRate',
        value: `${returnsRate.toFixed(1)}%`,
        icon: 'undo',
        tone: 'violet',
        flagCount: refunded,
      },
    ];
  }

  /**
   * Slices a page out of the matching orders.
   *
   * Clamps the requested page rather than returning an empty one: narrowing a filter can
   * leave the reader on a page that no longer exists, and showing them the last real page
   * beats showing them nothing.
   */
  private pageOf(matching: readonly OrderRow[], request: PageRequest): PageT<OrderRow> {
    const size = Math.max(1, request.count);
    const totalPages = Math.max(1, Math.ceil(matching.length / size));
    const pageNumber = Math.min(Math.max(0, request.page), totalPages - 1);
    const start = pageNumber * size;

    return {
      size,
      totalElements: matching.length,
      totalPages,
      pageNumber,
      content: matching.slice(start, start + size),
    };
  }

  /** `'$1,240.00'` → `1240`. */
  private amountOf(total: string): number {
    return Number(total.replace(/[^0-9.]/g, '')) || 0;
  }

  private spanInDays(range: DateRangeValue): number {
    if (!range.from || !range.to) {
      return 30;
    }
    const span = Math.round((range.to.getTime() - range.from.getTime()) / DAY_MS) + 1;
    return Math.max(1, span);
  }

  /** Movement scales gently with the period so the figure is not obviously static. */
  private delta(days: number, base: number): string {
    return `${(base * (30 / Math.max(days, 1)) ** 0.25).toFixed(1)}%`;
  }
}
