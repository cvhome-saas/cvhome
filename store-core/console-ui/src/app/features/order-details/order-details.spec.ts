import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, of, throwError} from 'rxjs';

import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import type {OrderStatus, ReadableOrder} from '@models/checkout';
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
  totals: [
    {code: 'order.subtotal', title: 'Subtotal', text: '$212.00'},
    {code: 'order.shipping', title: 'Shipping', text: '$9.00'},
    {code: 'order.total', title: 'Total', text: '$221.00'},
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

    // A store with a discount line gets it for free; the console does not have to know it exists.
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

  it('records a status change and re-reads the order rather than assuming', fakeAsync(() => {
    const element = load();
    const comment = element.querySelector('.status-form input') as HTMLInputElement;
    comment.value = 'Handed to courier';
    comment.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    (element.querySelector('.status-form button') as HTMLButtonElement).click();
    tick();
    fixture.detectChanges();

    expect(api.posted).toEqual([{status: 'PROCESSING', comments: 'Handed to courier'}]);
    // The server decides what the status becomes; a page that assumed would drift from it.
    expect(api.loads).toEqual([10482, 10482]);
  }));

  it('reports a failed status change instead of pretending it landed', fakeAsync(() => {
    api.postFails = true;
    const element = load();

    (element.querySelector('.status-form button') as HTMLButtonElement).click();
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
