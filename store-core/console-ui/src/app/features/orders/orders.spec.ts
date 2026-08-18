import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, Subject, of, throwError} from 'rxjs';

import {ORDERS} from '@mocks/orders.fixture';
import type {OrderRow, OrdersSnapshot} from '@models/orders';
import {translocoTesting} from '@testing/transloco-testing';
import {Orders} from './orders';
import {OrdersApi, type OrdersQuery} from './services/orders.api.service';
import {PAGE_SIZE} from './facades/orders.facade';

/** Pages the fixture the way the real service does, so the spec exercises real paging. */
function snapshot(query: OrdersQuery, book: readonly OrderRow[] = ORDERS): OrdersSnapshot {
  const matching = book.filter(
    (order) =>
      (query.tab === 'all' || order.status === query.tab) &&
      (query.channel === 'all' || order.channel === query.channel),
  );
  const size = query.page.count;
  const totalPages = Math.max(1, Math.ceil(matching.length / size));
  const pageNumber = Math.min(Math.max(0, query.page.page), totalPages - 1);

  return {
    kpis: [{labelKey: 'test.orders', value: `${matching.length}`, icon: 'shoppingCart', tone: 'blue'}],
    page: {
      size,
      totalElements: matching.length,
      totalPages,
      pageNumber,
      content: matching.slice(pageNumber * size, pageNumber * size + size),
    },
    totalInRange: book.length,
    lateCount: matching.filter((order) => order.unfulfilledFor).length,
  };
}

/** Stands in for the real endpoint so the spec controls timing and failure. */
class FakeOrdersApi {
  readonly requests: OrdersQuery[] = [];
  /** When set, requests hang until `resolve()` — used to observe the loading state. */
  deferred = false;
  pending: Subject<OrdersSnapshot> | null = null;
  failure: Error | null = null;

  loadOrders(query: OrdersQuery): Observable<OrdersSnapshot> {
    this.requests.push(query);
    if (this.failure) {
      return throwError(() => this.failure);
    }
    if (this.deferred) {
      this.pending = new Subject<OrdersSnapshot>();
      return this.pending;
    }
    return of(snapshot(query));
  }

  resolve(value: OrdersSnapshot): void {
    this.pending?.next(value);
    this.pending?.complete();
    this.pending = null;
  }

  get lastRequest(): OrdersQuery {
    return this.requests[this.requests.length - 1];
  }
}

/**
 * The page owns only its own content — the banner, rail and toolbar are covered by
 * `console-shell.spec.ts`.
 */
describe('Orders', () => {
  let api: FakeOrdersApi;

  beforeEach(async () => {
    api = new FakeOrdersApi();
    await TestBed.configureTestingModule({
      imports: [Orders, ...translocoTesting().imports],
      providers: [provideRouter([]), {provide: OrdersApi, useValue: api}, ...translocoTesting().providers],
    }).compileComponents();
  });

  /** Creates the page and settles the initial request. */
  function load(): {fixture: ComponentFixture<Orders>; element: HTMLElement} {
    const fixture = TestBed.createComponent(Orders);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return {fixture, element: fixture.nativeElement as HTMLElement};
  }

  /** Settles whatever the last interaction kicked off. */
  function settle(fixture: ComponentFixture<Orders>): void {
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
  }

  function rows(element: HTMLElement): HTMLElement[] {
    return Array.from(element.querySelectorAll('app-table-row'));
  }

  it('renders a page of orders and none of the console chrome', fakeAsync(() => {
    const {element} = load();

    expect(element.querySelector('app-kpi-grid')).not.toBeNull();
    expect(element.querySelector('app-data-table')).not.toBeNull();
    expect(rows(element).length).toBe(PAGE_SIZE);
    expect(element.querySelector('.order-id')?.textContent?.trim()).toBe(ORDERS[0].id);

    // Chrome belongs to the shell; a page must not grow its own.
    expect(element.querySelector('.toolbar')).toBeNull();
    expect(element.querySelector('.sidebar')).toBeNull();
    expect(element.querySelector('app-plan-banner')).toBeNull();
  }));

  it('asks for the first page of every channel and status on load', fakeAsync(() => {
    load();

    expect(api.requests.length).toBe(1);
    expect(api.lastRequest.tab).toBe('all');
    expect(api.lastRequest.channel).toBe('all');
    expect(api.lastRequest.page).toEqual({page: 0, count: PAGE_SIZE});
    expect(api.lastRequest.range.from).toBeTruthy();
  }));

  it('veils the table until the first response arrives', fakeAsync(() => {
    api.deferred = true;
    const fixture = TestBed.createComponent(Orders);
    settle(fixture);
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-busy-overlay')?.getAttribute('aria-busy')).toBe('true');
    expect(element.querySelector('.busy-veil')).not.toBeNull();
    expect(element.querySelector('app-data-table')).toBeNull();
    expect(element.querySelector('.busy-content')?.hasAttribute('inert')).toBeTrue();

    api.resolve(snapshot({...api.lastRequest}));
    tick();
    fixture.detectChanges();

    expect(element.querySelector('app-busy-overlay')?.getAttribute('aria-busy')).toBe('false');
    expect(element.querySelector('app-data-table')).not.toBeNull();
  }));

  it('refetches for the selected status and renders only those orders', fakeAsync(() => {
    const {fixture, element} = load();

    fixture.componentInstance['facade'].activeTab.set('Canceled');
    settle(fixture);

    expect(api.lastRequest.tab).toBe('Canceled');
    const canceled = ORDERS.filter((order) => order.status === 'Canceled');
    expect(rows(element).length).toBe(canceled.length);
  }));

  it('drops back to the first page when the filter changes', fakeAsync(() => {
    const {fixture} = load();

    fixture.componentInstance['facade'].goToPage(2);
    settle(fixture);
    expect(api.lastRequest.page.page).toBe(2);

    // Page 3 of "all" is not page 3 of "Delivered"; asking for it would strand the reader.
    fixture.componentInstance['facade'].activeTab.set('Delivered');
    settle(fixture);

    expect(api.lastRequest.tab).toBe('Delivered');
    expect(api.lastRequest.page.page).toBe(0);
  }));

  it('pages through the book from the pager', fakeAsync(() => {
    const {fixture, element} = load();

    const next = element.querySelector<HTMLButtonElement>('[aria-label="Next page"]');
    expect(next).not.toBeNull();
    next!.click();
    settle(fixture);

    expect(api.lastRequest.page.page).toBe(1);
    expect(element.querySelector('.order-id')?.textContent?.trim()).toBe(ORDERS[PAGE_SIZE].id);
  }));

  it('filters by channel', fakeAsync(() => {
    const {fixture, element} = load();

    fixture.componentInstance['facade'].channel.set('Phone');
    settle(fixture);

    expect(api.lastRequest.channel).toBe('Phone');
    const phone = ORDERS.filter((order) => order.channel === 'Phone');
    expect(rows(element).length).toBe(Math.min(PAGE_SIZE, phone.length));
  }));

  it('raises the overdue notice only on the queue those orders are stuck in', fakeAsync(() => {
    const {fixture, element} = load();

    expect(element.querySelector('app-notice-bar')).toBeNull();

    fixture.componentInstance['facade'].activeTab.set('Ordered');
    settle(fixture);

    const notice = element.querySelector('app-notice-bar');
    expect(notice).not.toBeNull();
    const late = ORDERS.filter(
      (order) => order.status === 'Ordered' && order.unfulfilledFor,
    ).length;
    expect(notice?.textContent).toContain(`${late} orders have been waiting`);

    fixture.componentInstance['facade'].activeTab.set('Delivered');
    settle(fixture);

    expect(element.querySelector('app-notice-bar')).toBeNull();
  }));

  it('keeps the previous rows on screen while the next filter loads', fakeAsync(() => {
    const {fixture, element} = load();
    expect(element.querySelector('.order-id')?.textContent?.trim()).toBe(ORDERS[0].id);

    api.deferred = true;
    fixture.componentInstance['facade'].activeTab.set('Delivered');
    settle(fixture);

    // Veiled, but still readable — the layout must not collapse mid-request.
    expect(element.querySelector('.busy-veil')).not.toBeNull();
    expect(element.querySelector('.order-id')?.textContent?.trim()).toBe(ORDERS[0].id);

    api.resolve(snapshot({...api.lastRequest}));
    tick();
    fixture.detectChanges();

    const delivered = ORDERS.find((order) => order.status === 'Delivered');
    expect(element.querySelector('.order-id')?.textContent?.trim()).toBe(delivered!.id);
  }));

  it('says so when a filter matches nothing', fakeAsync(() => {
    const {fixture, element} = load();

    api.deferred = true;
    fixture.componentInstance['facade'].activeTab.set('Refunded');
    settle(fixture);
    api.resolve(snapshot({...api.lastRequest}, []));
    tick();
    fixture.detectChanges();

    expect(element.querySelector('app-data-table')).toBeNull();
    expect(element.querySelector('.table-empty')?.textContent).toContain('No orders match');
  }));

  it('surfaces a failed request with a retry that refetches', fakeAsync(() => {
    api.failure = new Error('Unable to load orders.');
    const fixture = TestBed.createComponent(Orders);
    settle(fixture);
    const element = fixture.nativeElement as HTMLElement;

    const alert = element.querySelector('.load-error');
    expect(alert).not.toBeNull();
    expect(alert?.textContent).toContain('Unable to load orders.');

    api.failure = null;
    element.querySelector<HTMLButtonElement>('.load-error button')!.click();
    settle(fixture);

    expect(api.requests.length).toBe(2);
    expect(element.querySelector('.load-error')).toBeNull();
    expect(rows(element).length).toBe(PAGE_SIZE);
  }));
});
