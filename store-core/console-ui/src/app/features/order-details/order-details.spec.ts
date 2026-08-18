import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, of, throwError} from 'rxjs';

import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import type {OrderStatus, ReadableOrder} from '@models/checkout';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {OrderDetails} from './order-details';
import {OrderDetailsApi, type OrderDetail} from './services/order-details.api.service';

const ORDER: ReadableOrder = {
  id: 10482,
  orderStatus: 'PROCESSING',
  datePurchased: '2026-08-04T10:15:00Z',
  currency: 'USD',
  customerAgreed: true,
  confirmedAddress: false,
  paymentStatus: 'PAID',
  reservationStatus: 'RESERVED',
  customer: {id: 7, firstName: 'Maya', lastName: 'Chen', emailAddress: 'maya@northline.example'},
  products: [
    {id: 1, productName: 'Wireless Headphones', sku: 'ACME-HDPH-01', orderedQuantity: 2, price: '$62.00', subTotal: '$124.00'},
    {id: 2, productName: 'USB-C Dock', sku: 'ACME-DOCK-9', orderedQuantity: 1, price: '$88.00', subTotal: '$88.00'},
  ],
  // Shaped like the running stack: `title` and `text` are null on every total, so both the label
  // and the amount are derived by the console.
  totals: [
    {id: 1, code: 'order.total.subtotal', module: 'subtotal', title: undefined, text: undefined, value: 212},
    {id: 2, code: 'order.total.shipping', module: 'shipping', title: undefined, text: undefined, value: 9},
    {id: 3, code: 'order.total.total', module: 'total', title: undefined, text: undefined, value: 221},
  ],
  billing: {firstName: 'Maya', lastName: 'Chen', address: '18 Harrison St', city: 'San Francisco', postalCode: '94103', country: 'US', email: 'maya@northline.example'},
  delivery: {firstName: 'Maya', lastName: 'Chen', address: '18 Harrison St', city: 'San Francisco', postalCode: '94103', country: 'US'},
};

const DETAIL: OrderDetail = {
  order: ORDER,
  history: [
    {id: 1, orderStatus: 'CREATED', comments: 'Order placed', date: '2026-08-04T10:15:00Z'},
    {id: 2, orderStatus: 'PROCESSING', comments: 'Picking started', date: '2026-08-04T14:00:00Z'},
  ],
  countries: new Map([['US', 'United States']]),
};

class FakeOrderDetailsApi {
  loads: number[] = [];
  posted: {status: OrderStatus; comments: string}[] = [];
  detail: OrderDetail = DETAIL;
  failure = false;
  postFails = false;

  load(orderId: number): Observable<OrderDetail> {
    this.loads.push(orderId);
    return this.failure ? throwError(() => new Error('Unable to load order.')) : of(this.detail);
  }

  addStatus(_orderId: number, status: OrderStatus, comments: string): Observable<void> {
    this.posted.push({status, comments});
    return this.postFails ? throwError(() => new Error('nope')) : of(void 0);
  }
}

describe('OrderDetails', () => {
  let api: FakeOrderDetailsApi;
  let fixture: ComponentFixture<OrderDetails>;
  let toasts: {messages: string[]; danger(text: string): void};

  beforeEach(async () => {
    api = new FakeOrderDetailsApi();
    toasts = {messages: [], danger(text: string) { this.messages.push(text); }};
    await TestBed.configureTestingModule({
      imports: [OrderDetails, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        {provide: OrderDetailsApi, useValue: api},
        // The page names the store on the invoice and the operator on the composer.
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        {provide: NOTIFICATION_PORT, useValue: toasts},
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  function load(id = '10482'): HTMLElement {
    fixture = TestBed.createComponent(OrderDetails);
    fixture.componentRef.setInput('id', id);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('reads the order named by the route', fakeAsync(() => {
    load('10482');
    expect(api.loads).toEqual([10482]);
  }));

  it('renders every line with its quantity, unit price and line total', fakeAsync(() => {
    const element = load();
    const rows = [...element.querySelectorAll('.items tbody tr')];

    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('Wireless Headphones');
    expect(rows[0].textContent).toContain('ACME-HDPH-01');
    expect(rows[0].textContent).toContain('$62.00');
    expect(rows[0].textContent).toContain('$124.00');
  }));

  it('renders every total the server sent, not a fixed subtotal/tax/shipping trio', fakeAsync(() => {
    const element = load();
    const totals = [...element.querySelectorAll('.totals > div')].map((n) =>
      [...n.children].map((c) => c.textContent!.trim()).join(' '),
    );

    // Labelled from `module` and formatted from `value` + `currency`, because the server sends
    // neither a title nor formatted text. A discount line would appear here for free.
    expect(totals).toEqual(['Subtotal $212.00', 'Shipping $9.00', 'Total $221.00']);
  }));

  it('resolves the address country code to a name', fakeAsync(() => {
    const element = load();
    const billing = element.querySelectorAll('.address-grid section')[0];

    expect(billing.textContent).toContain('United States');
    expect(billing.textContent).not.toContain('>US<');
  }));

  it('says so when the order carries no delivery address', fakeAsync(() => {
    api.detail = {...DETAIL, order: {...ORDER, delivery: undefined}};
    const element = load();
    const delivery = element.querySelectorAll('.address-grid section')[1];

    expect(delivery.textContent).toContain('Not provided on this order.');
  }));

  it('tracks fulfilment from the order status, dating each stage from its history entry', fakeAsync(() => {
    const element = load();
    const stages = [...element.querySelectorAll('.stages li')];

    expect(stages.length).toBe(5);
    // PROCESSING is the third stage, so the first two are behind it and the last two ahead.
    expect(stages[0].className).toContain('done');
    expect(stages[2].className).toContain('current');
    expect(stages[4].className).toContain('todo');

    // The dates are real: stage one is dated from the CREATED history entry, not estimated.
    expect(stages[0].querySelector('.stage-meta')?.textContent?.trim()).toContain('Aug 4');
    expect(stages[4].querySelector('.stage-meta')?.textContent?.trim()).toBe('Not yet');
  }));

  it('does not date a stage the order has not reached, even when history has been there', fakeAsync(() => {
    // An order can move backwards: this one went out for delivery and came back to picking.
    api.detail = {
      ...DETAIL,
      order: {...ORDER, orderStatus: 'PROCESSING'},
      history: [
        ...DETAIL.history,
        {id: 3, orderStatus: 'DELIVERING', date: '2026-08-05T09:00:00Z'},
        {id: 4, orderStatus: 'PROCESSING', date: '2026-08-05T11:00:00Z'},
      ],
    };
    const element = load();
    const shipped = element.querySelectorAll('.stages li')[3];

    expect(shipped.className).toContain('todo');
    expect(shipped.querySelector('.stage-meta')?.textContent?.trim()).toBe('Not yet');
  }));

  it('replaces the tracker with a notice for an order that left the path', fakeAsync(() => {
    api.detail = {...DETAIL, order: {...ORDER, orderStatus: 'CANCELLED'}};
    const element = load();

    // A half-filled progress bar for a cancelled order would claim it is still moving.
    expect(element.querySelector('.stages')).toBeNull();
    expect(element.querySelector('.off-path')?.textContent).toContain('no longer moving');
  }));

  it('opens the invoice as a document and closes it again', fakeAsync(() => {
    const element = load();
    expect(element.querySelector('.invoice-sheet')).toBeNull();

    (element.querySelector('.invoice-action') as HTMLButtonElement).click();
    fixture.detectChanges();

    const sheet = element.querySelector('.invoice-sheet');
    expect(sheet).not.toBeNull();
    // The whole document, not a copy of the items panel: letterhead, both parties, lines, totals.
    expect(sheet!.querySelector('.seller')?.textContent).toContain('Acme Supply Co.');
    expect(sheet!.textContent).toContain('Billed to');
    expect(sheet!.textContent).toContain('Shipped to');
    expect(sheet!.querySelectorAll('.invoice-items tbody tr').length).toBe(2);
    expect(sheet!.querySelector('.invoice-totals .grand')?.textContent).toContain('$221.00');

    (element.querySelector('.invoice-close') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(element.querySelector('.invoice-sheet')).toBeNull();
  }));

  it('renders the timeline oldest first', fakeAsync(() => {
    const element = load();
    const entries = [...element.querySelectorAll('.timeline li')].map((n) => n.textContent!.trim());

    expect(entries.length).toBe(2);
    expect(entries[0]).toContain('Created');
    expect(entries[1]).toContain('Processing');
  }));

  it('humanizes a status the console has never seen rather than looking it up', fakeAsync(() => {
    // Transloco throws on a missing key, so a new server status must not be translated.
    api.detail = {...DETAIL, order: {...ORDER, orderStatus: 'SOMETHING_NEW' as OrderStatus}};
    const element = load();

    expect(element.textContent).toContain('Something New');
  }));

  it('shows the status it will actually submit', fakeAsync(() => {
    const element = load();
    const select = element.querySelector('.composer select') as HTMLSelectElement;

    // Binding `[value]` on the select left it displaying the first option while submitting another.
    expect(select.value).toBe('PROCESSING');
    expect(select.selectedOptions[0].textContent!.trim()).toBe('Processing');
  }));

  it('records a status change and re-reads the order rather than assuming', fakeAsync(() => {
    const element = load();
    const comment = element.querySelector('.composer textarea') as HTMLTextAreaElement;
    comment.value = 'Handed to courier';
    comment.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    (element.querySelector('.composer .primary-action') as HTMLButtonElement).click();
    tick();
    fixture.detectChanges();

    expect(api.posted).toEqual([{status: 'PROCESSING', comments: 'Handed to courier'}]);
    // The server decides what the status becomes; a page that assumed would drift from it.
    expect(api.loads).toEqual([10482, 10482]);
  }));

  it('reports a failed status change instead of pretending it landed', fakeAsync(() => {
    api.postFails = true;
    const element = load();

    (element.querySelector('.composer .primary-action') as HTMLButtonElement).click();
    tick();
    fixture.detectChanges();

    expect(toasts.messages.length).toBe(1);
    expect(api.loads).toEqual([10482]);
  }));

  it('offers no refund, capture, cancel or shipment control — none has an endpoint', fakeAsync(() => {
    const element = load();
    const text = element.textContent ?? '';

    for (const absent of ['Refund', 'Capture', 'Cancel order', 'Create shipment', 'Duplicate']) {
      expect(text).withContext(absent).not.toContain(absent);
    }
  }));

  it('surfaces a failed load with a retry', fakeAsync(() => {
    api.failure = true;
    const element = load();

    expect(element.querySelector('.load-error')).not.toBeNull();

    api.failure = false;
    (element.querySelector('.load-error button') as HTMLButtonElement).click();
    tick();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(element.querySelector('.load-error')).toBeNull();
    expect(element.querySelectorAll('.items tbody tr').length).toBe(2);
  }));
});
