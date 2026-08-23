import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, Subject, of, throwError} from 'rxjs';

import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import type {CustomerOrdersSnapshot, CustomerRow} from '@models/customers';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {Customers} from './customers';
import {CustomersApi, type CustomersQuery, type CustomersSnapshot} from './services/customers.api.service';
import {PAGE_SIZE} from './facades/customers.facade';

function row(id: number, name: string, overrides: Partial<CustomerRow> = {}): CustomerRow {
  return {
    id,
    name,
    email: `${name.split(' ')[0]!.toLowerCase()}@mail.com`,
    userName: '',
    company: 'Nordwerk',
    phone: '+48 22 118 4420',
    location: 'Warszawa, Poland',
    initials: 'NA',
    addresses: [{kind: 'billing', name, company: 'Nordwerk', phone: '', lines: ['ul. Prosta 51']}],
    ...overrides,
  };
}

const CUSTOMERS: readonly CustomerRow[] = [
  row(1, 'Marta Kowalska'),
  row(2, 'Tobias Lindqvist'),
  // The record the populator can give no name to: no billing address, so no first or last name.
  row(3, '', {email: 'nameless@mail.com', company: '', location: '', addresses: []}),
];

/** Stands in for the endpoints so the spec controls paging, filtering, timing and failure. */
class FakeCustomersApi {
  readonly requests: CustomersQuery[] = [];
  readonly orderRequests: number[] = [];
  /** When set, list requests hang until `resolve()` — used to observe the loading state. */
  pending: Subject<CustomersSnapshot> | null = null;
  failure = false;
  ordersFailure = false;
  customers: readonly CustomerRow[] = CUSTOMERS;
  orders: CustomerOrdersSnapshot = {
    rows: [
      {
        id: 10431,
        status: 'DELIVERED' as const,
        datePurchased: '2026-08-12T10:00:00Z',
        // `text` is null on every total the list endpoint sends, which is the case that matters.
        total: {value: 2410, currency: 'EUR', text: null},
      },
    ],
    totalElements: 12,
  };

  loadCustomers(query: CustomersQuery): Observable<CustomersSnapshot> {
    this.requests.push(query);
    if (this.failure) {
      return throwError(() => new Error('the customers could not be read'));
    }
    if (this.pending) {
      return this.pending;
    }
    return of(this.snapshot(query));
  }

  loadOrders(customerId: number): Observable<CustomerOrdersSnapshot> {
    this.orderRequests.push(customerId);
    if (this.ordersFailure) {
      return throwError(() => new Error('the orders could not be read'));
    }
    return of(this.orders);
  }

  resolve(query: CustomersQuery = this.requests[this.requests.length - 1]): void {
    const subject = this.pending;
    this.pending = null;
    subject?.next(this.snapshot(query));
    subject?.complete();
  }

  /** Filters and pages the way the server would, so the spec exercises real behaviour. */
  private snapshot(query: CustomersQuery): CustomersSnapshot {
    const term = query.search.trim().toLowerCase();
    // The server's `name` spans the billing name and the email; so does this.
    const matched = term
      ? this.customers.filter(
          (customer) =>
            customer.name.toLowerCase().includes(term) || customer.email.toLowerCase().includes(term),
        )
      : this.customers;

    const size = query.page.count;
    const totalPages = Math.max(1, Math.ceil(matched.length / size));
    const pageNumber = Math.min(Math.max(0, query.page.page), totalPages - 1);
    return {
      rows: matched.slice(pageNumber * size, pageNumber * size + size),
      totalElements: matched.length,
      totalPages,
      search: query.search,
    };
  }
}

describe('Customers', () => {
  let api: FakeCustomersApi;
  let fixture: ComponentFixture<Customers>;
  let router: Router;

  beforeEach(async () => {
    localStorage.removeItem('cvhome.console.store');
    api = new FakeCustomersApi();

    await TestBed.configureTestingModule({
      imports: [Customers, ...translocoTesting().imports],
      providers: [
        /*
         * The page mirrors its selection into `?customer=` and its term into `?q=`, and hands off to
         * the orders pages. Registering the real shapes means a navigation the page gets wrong fails
         * here instead of being swallowed.
         */
        provideRouter([
          {path: 'customers', children: []},
          {path: 'orders', children: []},
          {path: 'orders/:id', children: []},
        ]),
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        {provide: CustomersApi, useValue: api},
        {provide: NOTIFICATION_PORT, useValue: {danger: () => undefined}},
        ...translocoTesting().providers,
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
  });

  function load(): HTMLElement {
    fixture = TestBed.createComponent(Customers);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function settle(): void {
    tick();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
  }

  function rowsOnScreen(host: HTMLElement): HTMLElement[] {
    return Array.from(host.querySelectorAll('app-table-row'));
  }

  /*
   * The dialog is mounted for the life of the page and driven by `open`, so that it can animate
   * out — asking whether the element exists would answer "yes" forever. What matters is whether it
   * is showing.
   */
  function dialogIsOpen(host: HTMLElement): boolean {
    return host.querySelector('app-customer-dialog dialog[open]') !== null;
  }

  function openCustomer(host: HTMLElement, index: number): void {
    (rowsOnScreen(host)[index].querySelector('.identity') as HTMLButtonElement).click();
    settle();
  }

  it('lists the store customers, one row each', fakeAsync(() => {
    const host = load();

    expect(rowsOnScreen(host).length).toBe(3);
    expect(host.textContent).toContain('Marta Kowalska');
    expect(host.textContent).toContain('Warszawa, Poland');
  }));

  /* `count`, not Spring's `size`. The whole platform renames it, and this is the page's end of that. */
  it('asks for a page sized by count', fakeAsync(() => {
    load();

    expect(api.requests[0].page).toEqual({page: 0, count: PAGE_SIZE});
  }));

  /*
   * The record the populator cannot name. seller-ui renders it blank; this says so, which is the one
   * thing it adds — inventing a name from the email would put a value under a label the record does
   * not hold.
   */
  it('says so when a customer has no name rather than showing an empty cell', fakeAsync(() => {
    const host = load();
    const nameless = rowsOnScreen(host)[2];

    expect(nameless.querySelector('.unnamed')).not.toBeNull();
    expect(nameless.textContent).toContain('nameless@mail.com');
  }));

  it('sends the search term to the server and resets to the first page', fakeAsync(() => {
    const host = load();
    fixture.componentInstance['onPage'](1);
    settle();

    fixture.componentInstance['onSearch']('marta');
    settle();

    const last = api.requests[api.requests.length - 1];
    expect(last.search).toBe('marta');
    // Not page 1: a new term is a different result set, and page 2 of it is not where to land.
    expect(last.page.page).toBe(0);
    expect(rowsOnScreen(host).length).toBe(1);
  }));

  it('offers a different empty state for a search that matched nothing', fakeAsync(() => {
    const host = load();

    fixture.componentInstance['onSearch']('nobody at all');
    settle();

    expect(rowsOnScreen(host).length).toBe(0);
    expect(host.querySelector('app-empty-state')).not.toBeNull();
  }));

  /*
   * The whole reason a link from an order carries a term rather than an id: there is no endpoint
   * that fetches a customer by id, so landing on a single match has to open it.
   */
  it('opens the record when a term matches exactly one customer', fakeAsync(() => {
    const host = load();

    fixture.componentInstance['onSearch']('tobias');
    settle();

    expect(dialogIsOpen(host)).toBe(true);
    expect(api.orderRequests).toContain(2);
  }));

  /*
   * The case QA found: arriving with the term already in the URL, which is what a link from an
   * order details page is. The auto-open used to spend its one chance on the previous query's rows
   * — still on screen while the new request had not started — and the customer never opened.
   */
  it('opens the record when the term arrives in the URL rather than the box', fakeAsync(() => {
    fixture = TestBed.createComponent(Customers);
    fixture.componentRef.setInput('q', 'tobias');
    fixture.detectChanges();
    settle();
    settle();
    const host = fixture.nativeElement as HTMLElement;

    expect(dialogIsOpen(host)).toBe(true);
    expect(api.orderRequests).toContain(2);
  }));

  it('does not re-open the dialog after the operator closes it', fakeAsync(() => {
    const host = load();
    fixture.componentInstance['onSearch']('tobias');
    settle();

    fixture.componentInstance['closeDialog']();
    settle();

    expect(dialogIsOpen(host)).toBe(false);
  }));

  it('reads the open customer orders exactly once, on opening', fakeAsync(() => {
    const host = load();

    expect(api.orderRequests).toEqual([]);
    openCustomer(host, 0);

    expect(api.orderRequests).toEqual([1]);
    // The exact count comes off the response, not from the rows it returned.
    expect(host.textContent).toContain('12');
  }));

  it('keeps the record readable when its orders cannot be read', fakeAsync(() => {
    api.ordersFailure = true;
    const host = load();

    openCustomer(host, 0);

    expect(dialogIsOpen(host)).toBe(true);
    expect(host.querySelector('app-load-error')).not.toBeNull();
  }));

  it('hands off to the orders page for everything beyond the few it lists', fakeAsync(() => {
    const host = load();
    openCustomer(host, 0);
    const navigate = spyOn(router, 'navigate').and.resolveTo(true);

    fixture.componentInstance['viewAllOrders']();

    expect(navigate).toHaveBeenCalledWith(['/orders'], {queryParams: {customerId: 1}});
  }));

  it('offers a retry when the list itself fails', fakeAsync(() => {
    api.failure = true;
    const host = load();

    expect(host.querySelector('app-load-error')).not.toBeNull();
  }));
});
