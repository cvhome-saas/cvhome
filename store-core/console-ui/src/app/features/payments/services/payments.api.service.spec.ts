import {TestBed} from '@angular/core/testing';
import {Observable, of, throwError} from 'rxjs';

import {OrdersService} from '@api/orders/orders.service';
import {PaymentService} from '@api/payment/payment.service';
import type {PageT} from '@cvhome-saas/ui-kit';
import type {PaymentApproval, PaymentStatus, PaymentTransaction, TransactionQuery} from '@models/payment';
import type {ReadableOrder} from '@models/checkout';
import type {TransactionsSnapshot} from '@models/transactions';
import {PaymentsApi, type PaymentsQuery, type TransactionCounts} from './payments.api.service';

/** One transaction shaped exactly as `ReadableTransaction` sends it. */
const TRANSACTION: PaymentTransaction = {
  id: 41,
  internalRef: '2f2a9d18-6c4c-4f0e-9f7a-1c0a2b3d4e5f',
  requestRef: '10482',
  amount: 94,
  currency: {code: 'SAR'},
  paymentType: 'MANUAL_TRANSFER',
  status: 'PENDING',
  transactionDate: '2026-08-18T17:39:17Z',
  transactionNo: null,
};

function page(content: PaymentTransaction[]): PageT<PaymentTransaction> {
  return {size: 20, totalElements: content.length, totalPages: 1, pageNumber: 0, content};
}

type ListQuery = TransactionQuery & {page: number; count: number};

class FakePaymentService {
  queries: ListQuery[] = [];
  counts: TransactionQuery[] = [];
  failingStatuses: PaymentStatus[] = [];
  countFor = 7;
  approvals: {ref: string; approval: PaymentApproval}[] = [];
  rejections: string[] = [];
  transactionsInStore: PaymentTransaction[] = [TRANSACTION];

  transactions(query: ListQuery): Observable<PageT<PaymentTransaction>> {
    this.queries.push(query);
    return of(page(this.transactionsInStore));
  }

  countByStatus(status: PaymentStatus, range?: TransactionQuery): Observable<number> {
    this.counts.push({...range, status});
    if (this.failingStatuses.includes(status)) {
      return throwError(() => new Error(`${status} count is down`));
    }
    return of(this.countFor);
  }

  countAwaitingApproval(range?: TransactionQuery): Observable<number> {
    this.counts.push({...range, status: 'PENDING', paymentType: 'MANUAL_TRANSFER'});
    if (this.failingStatuses.includes('PENDING')) {
      return throwError(() => new Error('queue count is down'));
    }
    return of(3);
  }

  approve(ref: string, approval: PaymentApproval): Observable<void> {
    this.approvals.push({ref, approval});
    return of(undefined);
  }

  reject(ref: string): Observable<void> {
    this.rejections.push(ref);
    return of(undefined);
  }
}

class FakeOrdersService {
  loaded: (number | string)[] = [];

  get(orderId: number | string): Observable<ReadableOrder> {
    this.loaded.push(orderId);
    return of({id: Number(orderId)});
  }
}

function query(overrides: Partial<PaymentsQuery> = {}): PaymentsQuery {
  return {
    tab: 'all',
    gateway: '',
    search: '',
    page: {page: 0, count: 20},
    range: {from: null, to: null},
    ...overrides,
  };
}

describe('PaymentsApi', () => {
  let api: PaymentsApi;
  let payments: FakePaymentService;
  let orders: FakeOrdersService;

  beforeEach(() => {
    payments = new FakePaymentService();
    orders = new FakeOrdersService();
    TestBed.configureTestingModule({
      providers: [
        PaymentsApi,
        {provide: PaymentService, useValue: payments},
        {provide: OrdersService, useValue: orders},
      ],
    });
    api = TestBed.inject(PaymentsApi);
  });

  function load(overrides: Partial<PaymentsQuery> = {}): TransactionsSnapshot {
    let snapshot: TransactionsSnapshot | undefined;
    api.loadSnapshot(query(overrides)).subscribe((value) => (snapshot = value));
    return snapshot as TransactionsSnapshot;
  }

  /* -------------------------------------------------------------------- the filter ---- */

  it('sends no status and no gateway on the unfiltered tab', () => {
    load();
    expect(payments.queries[0].status).toBeUndefined();
    expect(payments.queries[0].paymentType).toBeUndefined();
  });

  it('sends the tab as the status when the tab is a status', () => {
    load({tab: 'FAILED'});
    expect(payments.queries[0].status).toBe('FAILED');
  });

  /*
   * The queue is the one compound tab, and it is not WAITING_VERIFICATION — nothing ever sets that
   * status, so filtering on it would return an empty queue forever.
   */
  it('asks for a pending manual transfer on the approval queue, not WAITING_VERIFICATION', () => {
    load({tab: 'queue'});
    expect(payments.queries[0].status).toBe('PENDING');
    expect(payments.queries[0].paymentType).toBe('MANUAL_TRANSFER');
  });

  /* Both write `paymentType`, so letting the select win would quietly redefine the queue. */
  it('does not let the gateway filter overwrite the queue tab', () => {
    load({tab: 'queue', gateway: 'STRIPE'});
    expect(payments.queries[0].paymentType).toBe('MANUAL_TRANSFER');
  });

  it('applies the gateway filter on every other tab', () => {
    load({tab: 'all', gateway: 'STRIPE'});
    expect(payments.queries[0].paymentType).toBe('STRIPE');
  });

  /* One box, two server fields: digits are an order id, anything else is an internal reference. */
  it('routes an all-digits search term to the order reference', () => {
    load({search: ' 10482 '});
    expect(payments.queries[0].requestRef).toBe('10482');
    expect(payments.queries[0].internalRef).toBeUndefined();
  });

  it('routes any other search term to the internal reference', () => {
    load({search: '2f2a9d18-6c4c'});
    expect(payments.queries[0].internalRef).toBe('2f2a9d18-6c4c');
    expect(payments.queries[0].requestRef).toBeUndefined();
  });

  /* The server binds `Instant`, so a bare yyyy-MM-dd would not bind at all. */
  it('widens a period to whole local days as ISO instants', () => {
    const from = new Date(2026, 7, 1, 13, 30);
    const to = new Date(2026, 7, 4, 9, 15);
    load({range: {from, to}});

    const sent = payments.queries[0];
    expect(new Date(sent.transactionDateFrom as string).getHours()).toBe(0);
    expect(new Date(sent.transactionDateTo as string).getHours()).toBe(23);
    expect(sent.transactionDateFrom as string).toMatch(/Z$/);
  });

  it('sends no date bounds when the period is open', () => {
    load();
    expect(payments.queries[0].transactionDateFrom).toBeUndefined();
    expect(payments.queries[0].transactionDateTo).toBeUndefined();
  });

  it('passes the page and its size through untouched', () => {
    load({page: {page: 2, count: 20}});
    expect(payments.queries[0].page).toBe(2);
    expect(payments.queries[0].count).toBe(20);
  });

  /* ---------------------------------------------------------------------- the rows ---- */

  it('reads an order id out of the request reference', () => {
    const row = load().page.content[0];
    expect(row.orderId).toBe(10482);
    expect(row.reference).toBe('10482');
  });

  /*
   * `requestRef` is an order id only by a checkout convention. Anything that does not parse must not
   * become a link, or the console would offer a route to an order that does not exist.
   */
  it('offers no order link when the reference does not read as an id', () => {
    payments.transactionsInStore = [{...TRANSACTION, requestRef: 'sub_1P9xyz'}];
    const row = load().page.content[0];
    expect(row.orderId).toBeNull();
    expect(row.reference).toBe('sub_1P9xyz');
  });

  it('offers no order link for a zero or negative reference', () => {
    payments.transactionsInStore = [{...TRANSACTION, requestRef: '0'}];
    expect(load().page.content[0].orderId).toBeNull();
  });

  /* The row carries the amount and its currency, not a rendered string, so it survives a language change. */
  it('keeps the amount and its currency apart', () => {
    const row = load().page.content[0];
    expect(row.amount).toEqual({value: 94, currency: 'SAR'});
  });

  it('marks a pending manual transfer actionable and a settled one not', () => {
    payments.transactionsInStore = [TRANSACTION, {...TRANSACTION, id: 42, status: 'PAID'}];
    const rows = load().page.content;
    expect(rows[0].actionable).toBe(true);
    expect(rows[1].actionable).toBe(false);
  });

  /*
   * The guard QA put here. `approve` sets PAID and fires PaymentPaidEvent whatever it is given, so a
   * pending Stripe payment with an Approve button is an invitation to tell checkout an order was
   * paid for which no money was taken — and nothing can refund it.
   */
  it('offers no action on a pending card payment, which the processor still holds', () => {
    payments.transactionsInStore = [{...TRANSACTION, paymentType: 'STRIPE'}];
    expect(load().page.content[0].actionable).toBe(false);
  });

  it('offers an action on cash on delivery, which a person also settles', () => {
    payments.transactionsInStore = [{...TRANSACTION, paymentType: 'COD'}];
    expect(load().page.content[0].actionable).toBe(true);
  });

  it('keeps the page envelope the server sent', () => {
    const {page: read} = load();
    expect(read.totalElements).toBe(1);
    expect(read.pageNumber).toBe(0);
  });

  /* ------------------------------------------------------------------- the counts ---- */

  it('reads four counts, and asks the queue for a pending manual transfer', () => {
    let counts: TransactionCounts | undefined;
    api.loadCounts({from: null, to: null}).subscribe((value) => (counts = value));

    expect(counts).toEqual({queue: 3, paid: 7, failed: 7, refunded: 7});
    expect(payments.counts.map((c) => c.status)).toEqual(['PENDING', 'PAID', 'FAILED', 'REFUNDED']);
    expect(payments.counts[0].paymentType).toBe('MANUAL_TRANSFER');
  });

  it('scopes every count to the same period as the table', () => {
    const from = new Date(2026, 7, 1, 13, 30);
    api.loadCounts({from, to: null}).subscribe();
    expect(payments.counts.every((c) => c.transactionDateFrom !== undefined)).toBe(true);
  });

  /*
   * A figure that cannot be read must be null, not zero. On a page whose point is an approval queue,
   * "nothing is waiting" is the most dangerous wrong answer available.
   */
  it('answers null for a count that fails, and keeps the other three', () => {
    payments.failingStatuses = ['PAID'];
    let counts: TransactionCounts | undefined;
    api.loadCounts({from: null, to: null}).subscribe((value) => (counts = value));

    expect(counts).toEqual({queue: 3, paid: null, failed: 7, refunded: 7});
  });

  /* -------------------------------------------------------------------- the writes ---- */

  /* The UUID, not `ReadableTransaction.id` — the mistake having a numeric key invites. */
  it('approves against the internal ref with the external transaction number', () => {
    api.approve(TRANSACTION.internalRef, {transactionNo: 'ACME-2291'}).subscribe();
    expect(payments.approvals).toEqual([
      {ref: TRANSACTION.internalRef, approval: {transactionNo: 'ACME-2291'}},
    ]);
  });

  it('rejects against the internal ref', () => {
    api.reject(TRANSACTION.internalRef).subscribe();
    expect(payments.rejections).toEqual([TRANSACTION.internalRef]);
  });

  /* The detail endpoint, not the list: only it populates `products` and `customer`. */
  it('reads the order behind a transaction for the summary', () => {
    api.loadOrder(10482).subscribe();
    expect(orders.loaded).toEqual([10482]);
  });
});
