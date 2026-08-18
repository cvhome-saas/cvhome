import {TestBed} from '@angular/core/testing';
import {Observable, of, throwError} from 'rxjs';

import {StatisticService} from '@api/analytics/statistic.service';
import {PaymentService} from '@api/payment/payment.service';
import type {DashboardSnapshot} from '@models/dashboard';
import type {StatisticList, StatisticRange} from '@models/statistics';
import {DashboardApi, type CompleteRange} from './dashboard.api.service';

function list(...entries: [string, number][]): StatisticList {
  return {entries: entries.map(([name, value]) => ({date: null, name, value}))};
}

class FakeStatisticService {
  orderRanges: StatisticRange[] = [];
  /** Successive answers to `orderStatistic`: the current window, then the preceding one. */
  orders: StatisticList[] = [list(['DELIVERED', 10]), list(['DELIVERED', 8])];
  countries: StatisticList = list(['DE', 6], ['US', 4]);
  products: StatisticList = list(['SKU-A', 3], ['SKU-B', 7]);

  orderStatistic(range: StatisticRange): Observable<StatisticList> {
    this.orderRanges.push(range);
    return of(this.orders[this.orderRanges.length - 1] ?? list());
  }

  customerStatistic(): Observable<StatisticList> {
    return of(this.countries);
  }

  productStatistic(): Observable<StatisticList> {
    return of(this.products);
  }
}

class FakePaymentService {
  count: number | null = 3;

  countByStatus(): Observable<number> {
    return this.count === null ? throwError(() => new Error('payments is down')) : of(this.count);
  }
}

/** A whole calendar month, so the preceding window is easy to reason about. */
const RANGE: CompleteRange = {from: new Date(2026, 5, 1), to: new Date(2026, 5, 30)};

describe('DashboardApi', () => {
  let statistics: FakeStatisticService;
  let payments: FakePaymentService;
  let api: DashboardApi;

  beforeEach(() => {
    statistics = new FakeStatisticService();
    payments = new FakePaymentService();
    TestBed.configureTestingModule({
      providers: [
        {provide: StatisticService, useValue: statistics},
        {provide: PaymentService, useValue: payments},
      ],
    });
    api = TestBed.inject(DashboardApi);
  });

  /** Every fake answers synchronously, so the snapshot is assigned before this returns. */
  function load(): DashboardSnapshot {
    let result: DashboardSnapshot | undefined;
    api.loadSnapshot(RANGE).subscribe((snapshot) => (result = snapshot));
    if (!result) {
      throw new Error('loadSnapshot did not emit');
    }
    return result;
  }

  it('widens the range to whole days, so the last day is not silently excluded', () => {
    load();
    const [current] = statistics.orderRanges;

    expect(new Date(current.fromDate).getHours()).toBe(0);
    expect(new Date(current.toDate).getHours()).toBe(23);
    expect(new Date(current.toDate).getDate()).toBe(30);
  });

  it('compares against the preceding window of equal length, inclusive of both ends', () => {
    load();
    const [, previous] = statistics.orderRanges;

    // June has 30 days, so the comparison window is all of May.
    expect(new Date(previous.fromDate).getMonth()).toBe(4);
    expect(new Date(previous.fromDate).getDate()).toBe(2);
    expect(new Date(previous.toDate).getMonth()).toBe(4);
    expect(new Date(previous.toDate).getDate()).toBe(31);
  });

  it('totals orders across statuses and reports the movement', () => {
    statistics.orders = [list(['DELIVERED', 6], ['PROCESSING', 6]), list(['DELIVERED', 8])];
    const snapshot = load();
    const orders = snapshot.kpis.find((kpi) => kpi.id === 'orders');

    expect(orders?.value).toBe('12');
    expect(orders?.delta).toBe('50.0%');
    expect(orders?.trend).toBe('up');
  });

  it('reports a fall as a fall', () => {
    statistics.orders = [list(['DELIVERED', 4]), list(['DELIVERED', 8])];
    const orders = load().kpis.find((kpi) => kpi.id === 'orders');

    expect(orders?.delta).toBe('50.0%');
    expect(orders?.trend).toBe('down');
  });

  it('omits the movement when the previous window was empty, rather than claiming a percentage', () => {
    statistics.orders = [list(['DELIVERED', 4]), list()];
    const orders = load().kpis.find((kpi) => kpi.id === 'orders');

    expect(orders?.delta).toBeUndefined();
  });

  it('reports revenue and low stock as having no figure at all', () => {
    const snapshot = load();

    for (const id of ['revenue', 'lowStock'] as const) {
      const kpi = snapshot.kpis.find((candidate) => candidate.id === id);
      expect(kpi?.value).withContext(id).toBeNull();
      expect(kpi?.flagKey).withContext(id).toBe('dashboard.kpi.unavailable');
    }
  });

  it('counts orders that are placed but not yet shipped', () => {
    statistics.orders = [
      list(['CREATED', 1], ['PENDING_PAYMENT', 2], ['PROCESSING', 3], ['SHIPPED', 40], ['DELIVERED', 50]),
      list(),
    ];
    const awaiting = load().attention.find((item) => item.labelKey.includes('awaitingFulfilment'));

    expect(awaiting?.count).toBe('6');
  });

  it('reports payments as unavailable, never as zero, when the service is down', () => {
    payments.count = null;
    const snapshot = load();
    const kpi = snapshot.kpis.find((candidate) => candidate.id === 'pendingPayments');

    expect(kpi?.value).toBeNull();
    expect(kpi?.flagKey).toBe('dashboard.kpi.unavailable');
    expect(snapshot.attention[0].count).toBeNull();
    // And the rest of the page still arrives.
    expect(snapshot.orderStatuses.length).toBeGreaterThan(0);
    expect(snapshot.products.length).toBeGreaterThan(0);
  });

  it('drops the needs-review flag when nothing is waiting', () => {
    payments.count = 0;
    const kpi = load().kpis.find((candidate) => candidate.id === 'pendingPayments');

    expect(kpi?.value).toBe('0');
    expect(kpi?.flagKey).toBeUndefined();
  });

  it('humanizes status names instead of translating them', () => {
    statistics.orders = [list(['PENDING_PAYMENT', 5], ['SOMETHING_NEW', 1]), list()];
    const labels = load().orderStatuses.map((status) => status.label);

    // A status the console has never seen must not throw — Transloco is configured to fail on a
    // missing key, so these can never be looked up.
    expect(labels).toEqual(['Pending Payment', 'Something New']);
  });

  it('gives a known status a stable tone and an unknown one a fallback', () => {
    statistics.orders = [list(['DELIVERED', 5], ['SOMETHING_NEW', 1]), list()];
    const [known, unknown] = load().orderStatuses;

    expect(known.tone).toBe('green');
    expect(unknown.tone).toBeDefined();
  });

  it('ranks products and countries by size, and carries the SKU through untouched', () => {
    const snapshot = load();

    expect(snapshot.products).toEqual([
      {sku: 'SKU-B', orders: 7},
      {sku: 'SKU-A', orders: 3},
    ]);
    expect(snapshot.customerSplit.map((slice) => slice.label)).toEqual(['DE', 'US']);
  });
});
