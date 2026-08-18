import {Injectable} from '@angular/core';
import {Observable, delay, of, throwError} from 'rxjs';

import {
  DASHBOARD_ATTENTION,
  DASHBOARD_KPIS,
  DASHBOARD_ORDER_STATUSES,
  DASHBOARD_PRODUCTS,
} from '@mocks/dashboard.fixture';
import type {DashboardSnapshot} from '@models/dashboard';
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

/** Metrics for the dashboard page. Console chrome is served by `ConsoleApi`. */
@Injectable({providedIn: 'root'})
export class DashboardApi {
  /**
   * Loads the reporting snapshot for a period.
   *
   * Stands in for a real endpoint: it takes time, and the figures depend on the range, so
   * changing the dates visibly changes every widget rather than re-rendering the same
   * numbers.
   */
  loadSnapshot(range: DateRangeValue): Observable<DashboardSnapshot> {
    const latency =
      MIN_LATENCY_MS + Math.random() * (MAX_LATENCY_MS - MIN_LATENCY_MS);

    if (Math.random() < FAILURE_RATE) {
      return throwError(() => new Error('Unable to load dashboard metrics.')).pipe(
        delay(latency),
      );
    }

    return of(this.snapshotFor(range)).pipe(delay(latency));
  }

  /**
   * Derives a snapshot from the period. Deterministic — the same range always returns the
   * same figures, so the UI is stable across reloads.
   */
  private snapshotFor(range: DateRangeValue): DashboardSnapshot {
    const days = this.spanInDays(range);
    // A 30-day period reproduces the baseline fixture; longer periods scale up.
    const scale = days / 30;

    const orderStatuses = DASHBOARD_ORDER_STATUSES.map((status) => ({
      ...status,
      value: Math.max(1, Math.round(status.value * scale)),
    }));

    const orders = orderStatuses.reduce((sum, status) => sum + status.value, 0);
    const revenue = Math.round(orders * 154.6);
    const pending = Math.max(1, Math.round(7 * scale));
    const lowStock = Math.max(1, Math.round(12 * scale));

    const kpis = DASHBOARD_KPIS.map((kpi) => {
      switch (kpi.id) {
        case 'revenue':
          return {...kpi, value: `$${revenue.toLocaleString()}`, delta: this.delta(days, 12.4)};
        case 'orders':
          return {...kpi, value: `${orders.toLocaleString()}`, delta: this.delta(days, 5.8)};
        case 'pendingPayments':
          return {...kpi, value: `${pending}`};
        default:
          return {...kpi, value: `${lowStock}`};
      }
    });

    const attention = DASHBOARD_ATTENTION.map((item) => ({
      ...item,
      count: `${Math.max(1, Math.round(Number(item.count) * scale))}`,
    }));

    const products = DASHBOARD_PRODUCTS.map((product) => ({
      ...product,
      sales: Math.max(1, Math.round(product.sales * scale)),
    }));

    // Longer periods skew towards returning customers.
    const newShare = Math.min(80, Math.max(35, Math.round(72 - days * 0.32)));

    return {
      kpis,
      attention,
      orderStatuses,
      products,
      customerSplit: [
        {labelKey: 'dashboard.customerSplit.new', value: newShare, tone: 'green'},
        {labelKey: 'dashboard.customerSplit.returning', value: 100 - newShare, tone: 'slate'},
      ],
    };
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
