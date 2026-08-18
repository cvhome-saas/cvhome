import {TestBed} from '@angular/core/testing';
import {Observable, of, throwError} from 'rxjs';

import {OrdersService, type OrderQuery} from '@api/orders/orders.service';
import {StatisticService} from '@api/analytics/statistic.service';
import type {PageT} from '@core/table/table.types';
import type {ReadableOrder} from '@models/checkout';
import type {OrdersSnapshot} from '@models/orders';
import type {StatisticList} from '@models/statistics';
import {OrdersApi, type OrdersQuery} from './orders.api.service';

/** One order shaped exactly as the list endpoint sends it — verified against the running stack. */
const LIST_ORDER: ReadableOrder = {
  id: 1,
  orderStatus: 'PENDING_PAYMENT',
  paymentStatus: 'PENDING',
  currency: 'SAR',
  datePurchased: '2026-08-18T17:39:17Z',
  // Null on the list, populated only on the detail endpoint.
  customer: undefined,
  products: undefined,
  billing: {firstName: 'Ashraf', lastName: 'Mahmoud', email: 'ashraf@example.com', city: 'Cairo', country: 'EG'},
  // Present but entirely empty, which is how an order with no delivery address arrives.
  delivery: {firstName: undefined, city: undefined, country: undefined},
  total: {id: 3, code: 'order.total.total', module: 'total', value: 9400},
  totals: [{id: 3, code: 'order.total.total', module: 'total', value: 9400}],
};

function page(content: ReadableOrder[]): PageT<ReadableOrder> {
  return {size: 10, totalElements: content.length, totalPages: 1, pageNumber: 0, content};
}

class FakeOrdersService {
  queries: OrderQuery[] = [];
  orders: ReadableOrder[] = [LIST_ORDER];

  list(query: OrderQuery): Observable<PageT<ReadableOrder>> {
    this.queries.push(query);
    return of(page(this.orders));
  }
}

class FakeStatisticService {
  failing = false;
  entries: [string, number][] = [['PENDING_PAYMENT', 4], ['RETURNED', 1], ['DELIVERED', 5]];

  orderStatistic(): Observable<StatisticList> {
    if (this.failing) {
      return throwError(() => new Error('order-statistic is down'));
    }
    return of({entries: this.entries.map(([name, value]) => ({date: null, name, value}))});
  }
}

const QUERY: OrdersQuery = {
  tab: 'all',
  search: '',
  page: {page: 0, count: 10},
  range: {from: new Date(2026, 7, 1), to: new Date(2026, 7, 18)},
};

describe('OrdersApi', () => {
  let orders: FakeOrdersService;
  let statistics: FakeStatisticService;
  let api: OrdersApi;

  beforeEach(() => {
    orders = new FakeOrdersService();
    statistics = new FakeStatisticService();
    TestBed.configureTestingModule({
      providers: [
        {provide: OrdersService, useValue: orders},
        {provide: StatisticService, useValue: statistics},
      ],
    });
    api = TestBed.inject(OrdersApi);
  });

  function load(query: OrdersQuery = QUERY): OrdersSnapshot {
    let result: OrdersSnapshot | undefined;
    api.loadSnapshot(query).subscribe((snapshot) => (result = snapshot));
    if (!result) {
      throw new Error('loadSnapshot did not emit');
    }
    return result;
  }

  it('names the customer from the billing address, which is where the list puts them', () => {
    const [row] = load().page.content;

    // `customer` is null on every row of the list response; the buyer is on `billing`.
    expect(row.customer).toBe('Ashraf Mahmoud');
    expect(row.email).toBe('ashraf@example.com');
  });

  it('falls back to the billing city when the order has no delivery address', () => {
    // `delivery` arrives as an object with every field null rather than absent.
    expect(load().page.content[0].city).toBe('Cairo');
  });

  it('formats the total itself, because the server sends no formatted text', () => {
    const [row] = load().page.content;

    expect(row.total).toContain('9,400');
    expect(row.total).not.toBe('—');
  });

  it('routes a search term to the server field its shape implies', () => {
    load({...QUERY, search: 'maya@example.com'});
    load({...QUERY, search: '+20 100 555'});
    load({...QUERY, search: 'Maya Chen'});

    // Sending one term as all three would AND them and match nothing.
    expect(orders.queries[0].email).toBe('maya@example.com');
    expect(orders.queries[0].name).toBeUndefined();
    expect(orders.queries[1].phone).toBe('+20 100 555');
    expect(orders.queries[2].name).toBe('Maya Chen');
  });

  it('sends the status only when one is selected', () => {
    load({...QUERY, tab: 'all'});
    load({...QUERY, tab: 'DELIVERED'});

    expect(orders.queries[0].status).toBeUndefined();
    expect(orders.queries[1].status).toBe('DELIVERED');
  });

  it('counts awaiting-fulfilment orders across every pre-shipment status', () => {
    const awaiting = load().kpis.find((kpi) => kpi.labelKey.includes('awaitingFulfilment'));

    // PENDING_PAYMENT is one of the four; DELIVERED and RETURNED are not.
    expect(awaiting?.value).toBe('4');
  });

  it('keeps the table when the statistic endpoint fails, and reports the tiles as unavailable', () => {
    statistics.failing = true;
    const snapshot = load();

    // The whole point of making that leg optional: a defect in a different endpoint must not take
    // the order book down with it.
    expect(snapshot.page.content.length).toBe(1);

    const orderCount = snapshot.kpis.find((kpi) => kpi.labelKey.endsWith('.orders'));
    expect(orderCount?.value).toBe('1');

    for (const label of ['awaitingFulfilment', 'returnsRate']) {
      const kpi = snapshot.kpis.find((candidate) => candidate.labelKey.includes(label));
      expect(kpi?.value).withContext(label).toBeNull();
      expect(kpi?.flagKey).withContext(label).toBe('orders.kpi.unavailable');
    }
  });

  it('reports average order value as having no source at all', () => {
    const average = load().kpis.find((kpi) => kpi.labelKey.includes('averageOrderValue'));

    expect(average?.value).toBeNull();
    expect(average?.flagKey).toBe('orders.kpi.unavailable');
  });
});
