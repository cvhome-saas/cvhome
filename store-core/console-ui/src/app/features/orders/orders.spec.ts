import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, Subject, of, throwError} from 'rxjs';

import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import type {OrderStatus} from '@models/checkout';
import type {OrderRow, OrdersSnapshot} from '@models/orders';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {Orders} from './orders';
import {OrdersApi, type OrdersQuery} from './services/orders.api.service';
import {PAGE_SIZE} from './facades/orders.facade';

function row(id: number, status: OrderStatus, customer = 'Maya Chen'): OrderRow {
  return {
    id,
    reference: `#${id}`,
    customer,
    email: `${customer.split(' ')[0].toLowerCase()}@example.com`,
    city: 'Berlin',
    status,
    payment: 'PAID',
    total: {value: 124, currency: 'USD', text: null},
    placedOn: '2026-08-18T09:00:00Z',
  };
}

const BOOK: readonly OrderRow[] = [
  row(10, 'CREATED'),
  row(11, 'PROCESSING', 'Tobias Lind'),
  row(12, 'DELIVERED', 'Amina Haddad'),
  row(13, 'DELIVERED', 'Daniel Okoye'),
  row(14, 'CANCELLED', 'Camille Roux'),
];

/** Stands in for the endpoint so the spec controls filtering, paging, timing and failure. */
class FakeOrdersApi {
  readonly requests: OrdersQuery[] = [];
  /** When set, requests hang until `resolve()` — used to observe the loading state. */
  pending: Subject<OrdersSnapshot> | null = null;
  failure = false;
  book: readonly OrderRow[] = BOOK;

  loadSnapshot(query: OrdersQuery): Observable<OrdersSnapshot> {
    this.requests.push(query);
    if (this.failure) {
      return throwError(() => new Error('Unable to load orders.'));
    }
    return this.pending ?? of(this.snapshot(query));
  }

  resolve(query: OrdersQuery = this.requests[this.requests.length - 1]): void {
    const subject = this.pending;
    this.pending = null;
    subject?.next(this.snapshot(query));
    subject?.complete();
  }

  /** Filters and pages the book the way the server would, so the spec exercises real behaviour. */
  private snapshot(query: OrdersQuery): OrdersSnapshot {
    const term = query.search.trim().toLowerCase();
    const matching = this.book.filter(
      (order) =>
        (query.tab === 'all' || order.status === query.tab) &&
        (!term || order.customer.toLowerCase().includes(term) || order.email.includes(term)),
    );
    const size = query.page.count;
    const totalPages = Math.max(1, Math.ceil(matching.length / size));
    const pageNumber = Math.min(Math.max(0, query.page.page), totalPages - 1);

    return {
      kpis: [
        {labelKey: 'orders.kpi.orders', value: `${matching.length}`, icon: 'shoppingCart', tone: 'blue'},
        // The tile that has no source anywhere on the platform.
        {labelKey: 'orders.kpi.averageOrderValue', value: null, icon: 'chartLine', tone: 'slate', flagKey: 'orders.kpi.unavailable'},
      ],
      page: {
        size,
        totalElements: matching.length,
        totalPages,
        pageNumber,
        content: matching.slice(pageNumber * size, pageNumber * size + size),
      },
    };
  }
}

describe('Orders', () => {
  let api: FakeOrdersApi;
  let fixture: ComponentFixture<Orders>;

  beforeEach(async () => {
    localStorage.removeItem('cvhome.console.store');
    api = new FakeOrdersApi();
    await TestBed.configureTestingModule({
      imports: [Orders, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        {provide: OrdersApi, useValue: api},
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  function load(): HTMLElement {
    fixture = TestBed.createComponent(Orders);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function references(element: HTMLElement): string[] {
    return [...element.querySelectorAll('.order-ref')].map((node) => node.textContent!.trim());
  }

  it('renders a page of orders and none of the console chrome', fakeAsync(() => {
    const element = load();

    expect(references(element)).toEqual(['#10', '#11', '#12', '#13', '#14']);
    expect(element.querySelector('.toolbar')).toBeNull();
    expect(element.querySelector('.sidebar')).toBeNull();
  }));

  it('asks for the first page of every status on load', fakeAsync(() => {
    load();

    expect(api.requests.length).toBe(1);
    expect(api.requests[0].tab).toBe('all');
    expect(api.requests[0].search).toBe('');
    expect(api.requests[0].page).toEqual({page: 0, count: PAGE_SIZE});
  }));

  it('offers a way back when a filter empties the table, and none when the period is simply empty', fakeAsync(() => {
    api.book = [];
    const element = load();

    // Nothing placed in the period: there is nothing for the operator to undo.
    expect(element.querySelector('.table-empty')?.textContent).toContain('No orders were placed');
    expect(element.querySelector('.table-empty button')).toBeNull();

    const search = element.querySelector('.order-search input') as HTMLInputElement;
    search.value = 'nobody@example.com';
    search.dispatchEvent(new Event('change'));
    tick();
    fixture.detectChanges();

    const clear = element.querySelector('.table-empty button') as HTMLButtonElement;
    expect(element.querySelector('.table-empty')?.textContent).toContain('No orders match');
    expect(clear).not.toBeNull();

    clear.click();
    tick();
    fixture.detectChanges();
    expect(search.value).toBe('');
  }));

  it('offers a tab for every real status, not the five the mockup drew', fakeAsync(() => {
    const element = load();
    const tabs = [...element.querySelectorAll('app-tab-switcher button')].map((b) => b.textContent!.trim());

    // Ten statuses plus "All", each read from the `status.*` dictionary — "Pending payment", not
    // the enum humanized into "Pending Payment".
    expect(tabs.length).toBe(11);
    expect(tabs).toContain('Pending payment');
    expect(tabs).toContain('Delivering');
    expect(tabs).not.toContain('Ordered');
  }));

  it('refetches for the selected status and renders only those orders', fakeAsync(() => {
    const element = load();
    const delivered = [...element.querySelectorAll('app-tab-switcher button')].find(
      (b) => b.textContent!.trim() === 'Delivered',
    ) as HTMLButtonElement;

    delivered.click();
    tick();
    fixture.detectChanges();

    expect(api.requests[api.requests.length - 1].tab).toBe('DELIVERED');
    expect(references(element)).toEqual(['#12', '#13']);
  }));

  it('sends the search term to the server rather than filtering on screen', fakeAsync(() => {
    const element = load();
    const search = element.querySelector('.order-search input') as HTMLInputElement;

    search.value = 'Tobias';
    search.dispatchEvent(new Event('change'));
    tick();
    fixture.detectChanges();

    expect(api.requests[api.requests.length - 1].search).toBe('Tobias');
    expect(references(element)).toEqual(['#11']);
  }));

  it('drops back to the first page when the filter changes', fakeAsync(() => {
    api.book = Array.from({length: 25}, (_, index) => row(100 + index, 'CREATED'));
    const element = load();

    const steps = element.querySelectorAll('app-pagination .page-step');
    (steps[steps.length - 1] as HTMLButtonElement).click();
    tick();
    fixture.detectChanges();
    expect(api.requests[api.requests.length - 1].page.page).toBe(1);

    const cancelled = [...element.querySelectorAll('app-tab-switcher button')].find(
      (b) => b.textContent!.trim() === 'Cancelled',
    ) as HTMLButtonElement;
    cancelled.click();
    tick();
    fixture.detectChanges();

    // Holding page 2 would ask for a page the narrowed result does not have.
    expect(api.requests[api.requests.length - 1].page.page).toBe(0);
  }));

  it('refetches when the open store changes', fakeAsync(() => {
    load();
    expect(api.requests.length).toBe(1);

    TestBed.inject(ConsoleShellFacade).selectStore(CONSOLE_STORES_FAKE[1].id);
    tick();

    // Orders belong to one store; the table must not outlive the store it was read for.
    expect(api.requests.length).toBe(2);
  }));

  it('shows an em dash for a figure with no source, never a zero', fakeAsync(() => {
    const element = load();
    const cards = [...element.querySelectorAll('app-kpi-card')] as HTMLElement[];
    const average = cards[cards.length - 1];

    expect(average.querySelector('.kpi-value')?.textContent?.trim()).toBe('—');
    expect(average.textContent).toContain('Not available yet');
  }));

  it('keeps the previous rows on screen while the next filter loads', fakeAsync(() => {
    const element = load();
    api.pending = new Subject<OrdersSnapshot>();

    const cancelled = [...element.querySelectorAll('app-tab-switcher button')].find(
      (b) => b.textContent!.trim() === 'Cancelled',
    ) as HTMLButtonElement;
    cancelled.click();
    fixture.detectChanges();

    // Blanking the table on every tab change would make the page flicker.
    expect(references(element).length).toBe(5);
    expect(element.querySelector('app-busy-overlay .busy-veil')).not.toBeNull();

    api.resolve();
    tick();
    fixture.detectChanges();
    expect(references(element)).toEqual(['#14']);
  }));

  it('says so when a filter matches nothing', fakeAsync(() => {
    api.book = [];
    const element = load();

    expect(element.querySelector('app-data-table')).toBeNull();
    expect(element.querySelector('.table-empty')).not.toBeNull();
  }));

  it('surfaces a failed request with a retry that refetches', fakeAsync(() => {
    api.failure = true;
    const element = load();

    expect(element.querySelector('.load-error')).not.toBeNull();

    api.failure = false;
    (element.querySelector('.load-error button') as HTMLButtonElement).click();
    tick();
    fixture.detectChanges();

    expect(element.querySelector('.load-error')).toBeNull();
    expect(references(element).length).toBe(5);
  }));

  it('opens an order by navigating to it', fakeAsync(() => {
    const navigate = spyOn(TestBed.inject(Router), 'navigate');
    const element = load();

    (element.querySelector('.order-ref') as HTMLButtonElement).click();

    expect(navigate).toHaveBeenCalledWith(['/orders', 10]);
  }));
});
