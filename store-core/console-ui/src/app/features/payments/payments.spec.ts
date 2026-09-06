import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, Subject, of, throwError} from 'rxjs';

import {NOTIFICATION_PORT} from '@cvhome-saas/ui-kit';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import type {ReadableOrder} from '@models/checkout';
import type {PaymentStatus} from '@models/payment';
import type {OrderKey, TransactionRow, TransactionsSnapshot} from '@models/transactions';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {Payments} from './payments';
import {PaymentsApi, type PaymentsQuery, type TransactionCounts} from './services/payments.api.service';
import {PAGE_SIZE} from './facades/payments.facade';

function row(
  id: number,
  status: PaymentStatus,
  paymentType = 'MANUAL_TRANSFER',
  requestRef = String(10480 + id),
): TransactionRow {
  return {
    id,
    internalRef: `ref-${id}`,
    transactionNo: status === 'PAID' ? `ACME-${id}` : null,
    orderId: /^\d+$/.test(requestRef) ? Number(requestRef) : null,
    orderRef: /^[0-9a-f-]{36}$/.test(requestRef) ? requestRef : null,
    reference: requestRef,
    paymentType,
    status,
    amount: {value: 100 + id, currency: 'SAR'},
    placedOn: '2026-08-18T09:00:00Z',
    actionable:
      ['PENDING', 'PROCESSING', 'WAITING_VERIFICATION', 'AUTHORIZED'].includes(status) &&
      ['MANUAL_TRANSFER', 'COD'].includes(paymentType),
  };
}

const LEDGER: readonly TransactionRow[] = [
  row(1, 'PENDING'),
  row(2, 'PENDING', 'STRIPE'),
  row(3, 'PAID', 'STRIPE'),
  row(4, 'FAILED', 'STRIPE'),
  row(5, 'REFUNDED', 'COD'),
];

/** Stands in for the endpoint so the spec controls filtering, paging, timing and failure. */
class FakePaymentsApi {
  readonly requests: PaymentsQuery[] = [];
  readonly approvals: {ref: string; transactionNo: string}[] = [];
  readonly rejections: string[] = [];
  readonly orderLoads: OrderKey[] = [];
  /** When set, list requests hang until `resolve()` — used to observe the loading state. */
  pending: Subject<TransactionsSnapshot> | null = null;
  failure = false;
  ledger: readonly TransactionRow[] = LEDGER;
  counts: TransactionCounts = {queue: 2, paid: 1, failed: 1, refunded: 1};
  countCalls = 0;

  loadSnapshot(query: PaymentsQuery): Observable<TransactionsSnapshot> {
    this.requests.push(query);
    if (this.failure) {
      return throwError(() => new Error('Unable to load payments.'));
    }
    return this.pending ?? of(this.snapshot(query));
  }

  loadOrder(key: OrderKey): Observable<ReadableOrder> {
    this.orderLoads.push(key);
    return of({
      id: key.id ?? 4187,
      orderRef: key.ref ?? undefined,
      orderStatus: 'PROCESSING' as const,
      currency: 'SAR',
      datePurchased: '2026-08-18T09:00:00Z',
      customer: {firstName: 'Maya', lastName: 'Chen', emailAddress: 'maya@example.com'},
      products: [
        {id: 1, productName: 'Chanel Ballerinas', orderedQuantity: 2, subTotal: '1700.00'},
      ],
      total: {id: 3, code: 'order.total.total', module: 'total', value: 1700},
    });
  }

  loadCounts(): Observable<TransactionCounts> {
    this.countCalls += 1;
    return of(this.counts);
  }

  approve(ref: string, approval: {transactionNo: string}): Observable<void> {
    this.approvals.push({ref, transactionNo: approval.transactionNo});
    return of(undefined);
  }

  reject(ref: string): Observable<void> {
    this.rejections.push(ref);
    return of(undefined);
  }

  resolve(query: PaymentsQuery = this.requests[this.requests.length - 1]): void {
    const subject = this.pending;
    this.pending = null;
    subject?.next(this.snapshot(query));
    subject?.complete();
  }

  /** Filters and pages the ledger the way the server would, so the spec exercises real behaviour. */
  private snapshot(query: PaymentsQuery): TransactionsSnapshot {
    const term = query.search.trim();
    const matching = this.ledger.filter((entry) => {
      if (query.tab === 'queue') {
        return entry.status === 'PENDING' && entry.paymentType === 'MANUAL_TRANSFER';
      }
      if (query.tab !== 'all' && entry.status !== query.tab) {
        return false;
      }
      if (query.gateway && entry.paymentType !== query.gateway) {
        return false;
      }
      return !term || entry.reference.includes(term) || entry.internalRef.includes(term);
    });

    const size = query.page.count;
    const totalPages = Math.max(1, Math.ceil(matching.length / size));
    const pageNumber = Math.min(Math.max(0, query.page.page), totalPages - 1);

    return {
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

describe('Payments', () => {
  let api: FakePaymentsApi;
  let fixture: ComponentFixture<Payments>;
  /* `ApiErrorService` reports failures through this port; the facade's own toasts go via ToastService. */
  let toasts: {messages: string[]; danger(text: string): void};

  beforeEach(async () => {
    localStorage.removeItem('cvhome.console.store');
    api = new FakePaymentsApi();
    toasts = {messages: [], danger(text: string) { this.messages.push(text); }};
    await TestBed.configureTestingModule({
      imports: [Payments, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        {provide: PaymentsApi, useValue: api},
        {provide: NOTIFICATION_PORT, useValue: toasts},
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  function load(): HTMLElement {
    fixture = TestBed.createComponent(Payments);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    // A second cycle: the table and the counts are two resources, and the body is behind the
    // overlay's `@if (!isEmpty())` until the first of them has landed.
    tick();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  /*
   * Two cycles, for the same reason `load` needs them: the table and the counts are separate
   * resources, and a filter change settles the first on one turn and the view on the next.
   */
  function settle(): void {
    tick();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
  }

  function references(element: HTMLElement): string[] {
    return [...element.querySelectorAll('.cell-transaction app-copy-field')].map((node) =>
      node.textContent!.trim(),
    );
  }

  function clickTab(element: HTMLElement, label: string): void {
    const tab = [...element.querySelectorAll<HTMLButtonElement>('[role="tab"]')].find(
      (node) => node.textContent!.trim().startsWith(label),
    );
    tab!.click();
    settle();
  }

  /* --------------------------------------------------------------------- the page ---- */

  /*
   * The queue leads, because it is the only tab that is a to-do list rather than a view. Opening on
   * "All" would bury the one thing an operator has to act on.
   */
  it('opens on the approval queue, not on every transaction', fakeAsync(() => {
    const element = load();

    expect(api.requests[0].tab).toBe('queue');
    expect(references(element)).toEqual(['ref-1']);
    expect(element.querySelector('.toolbar')).toBeNull();
    expect(element.querySelector('.sidebar')).toBeNull();
  }));

  it('asks for the first page once on load', fakeAsync(() => {
    load();

    expect(api.requests.length).toBe(1);
    expect(api.requests[0].page).toEqual({page: 0, count: PAGE_SIZE});
  }));

  it('shows every transaction on the All tab', fakeAsync(() => {
    const element = load();
    clickTab(element, 'All');

    expect(references(element)).toEqual(['ref-1', 'ref-2', 'ref-3', 'ref-4', 'ref-5']);
  }));

  /* --------------------------------------------------------------------- the KPIs ---- */

  /*
   * The KPI row is keyed on the store and the period, never on the tab. Four extra requests per tab
   * click for four figures that had not moved is the cost this keying avoids.
   */
  it('does not re-read the counts when the tab changes', fakeAsync(() => {
    const element = load();
    expect(api.countCalls).toBe(1);

    clickTab(element, 'All');
    clickTab(element, 'Failed');

    expect(api.requests.length).toBe(3);
    expect(api.countCalls).toBe(1);
  }));

  /* An em dash, never a zero: "nothing is waiting" is the wrong answer this page must not give. */
  it('shows an em dash rather than a zero for a count that could not be read', fakeAsync(() => {
    api.counts = {queue: null, paid: 4, failed: null, refunded: 0};
    const element = load();

    const tiles = [...element.querySelectorAll('app-kpi-card')].map((node) => node.textContent!);
    expect(tiles[0]).toContain('—');
    expect(tiles[0]).toContain('Not available');
    // A genuine zero is a figure, and says so differently.
    expect(tiles[3]).toContain('0');
    expect(tiles[3]).not.toContain('—');
  }));

  it('badges the queue tab with the number waiting', fakeAsync(() => {
    const element = load();

    const queueTab = element.querySelector<HTMLElement>('[role="tab"]')!;
    expect(queueTab.textContent).toContain('2');
  }));

  /* ------------------------------------------------------------------ the order link ---- */

  /*
   * A summary rather than a navigation: leaving the ledger to answer "what did this pay for" would
   * cost the operator their filter, their page and their place.
   */
  it('opens a summary of the order the reference names, without leaving the page', fakeAsync(() => {
    const element = load();
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    element.querySelector<HTMLButtonElement>('.order-ref')!.click();
    settle();

    expect(router.navigate).not.toHaveBeenCalled();
    expect(api.orderLoads).toEqual([{id: 10481, ref: null}]);

    const dialog = element.querySelector<HTMLDialogElement>('dialog.summary')!;
    expect(dialog.open).toBe(true);
    expect(dialog.textContent).toContain('#10481');
    expect(dialog.textContent).toContain('Maya Chen');
    expect(dialog.textContent).toContain('Chanel Ballerinas');
    expect(dialog.textContent).toContain('Processing');
  }));

  /*
   * The one value in the row a merchant can match against their own statement. Unlabelled it read
   * as a stray word under a UUID.
   */
  it('names the external reference rather than printing it bare', fakeAsync(() => {
    api.ledger = [{...row(1, 'PAID'), transactionNo: 'ACME-2291'}];
    const element = load();
    clickTab(element, 'All');

    const external = element.querySelector('.transaction-external')!;
    expect(external.querySelector('.transaction-external-label')!.textContent!.trim()).toBe('Ref');
    expect(external.querySelector('.transaction-external-value')!.textContent!.trim()).toBe('ACME-2291');
    expect(external.querySelector('.transaction-external-value')!.getAttribute('title')).toContain('bank statement');
  }));

  /* A transfer awaiting confirmation has no external reference yet, and must not show an empty chip. */
  it('shows no reference line before one exists', fakeAsync(() => {
    api.ledger = [{...row(1, 'PENDING'), transactionNo: null}];
    const element = load();
    clickTab(element, 'All');

    expect(element.querySelector('.transaction-external')).toBeNull();
  }));

  /* The icon is what tells the operator the reference does anything at all. */
  it('marks the order reference as something that opens', fakeAsync(() => {
    const element = load();

    const trigger = element.querySelector<HTMLButtonElement>('.order-ref')!;
    expect(trigger.querySelector('.order-ref-icon')).not.toBeNull();
    expect(trigger.getAttribute('aria-label')).toContain('10481');
  }));

  /*
   * The regression QA found: it opened once, and after that the order reference did nothing. The
   * dialog closed itself imperatively, so the parent's `open` stayed true and the effect had no new
   * value to react to.
   */
  it('reopens after being closed', fakeAsync(() => {
    const element = load();
    const trigger = () => element.querySelector<HTMLButtonElement>('.order-ref')!;
    const dialog = () => element.querySelector<HTMLDialogElement>('dialog.summary')!;

    trigger().click();
    settle();
    expect(dialog().open).toBe(true);

    dialog().querySelector<HTMLButtonElement>('.summary-close')!.click();
    settle();
    expect(dialog().open).toBe(false);

    trigger().click();
    settle();
    expect(dialog().open).toBe(true);
  }));

  /* Escape and the backdrop are the platform's dismissals, and must clear the state too. */
  it('clears its state when the platform closes it', fakeAsync(() => {
    const element = load();
    element.querySelector<HTMLButtonElement>('.order-ref')!.click();
    settle();

    const dialog = element.querySelector<HTMLDialogElement>('dialog.summary')!;
    dialog.dispatchEvent(new Event('cancel'));
    settle();

    expect(dialog.open).toBe(false);
    element.querySelector<HTMLButtonElement>('.order-ref')!.click();
    settle();
    expect(dialog.open).toBe(true);
  }));

  /*
   * No approve, no reject, no invoice — the summary answers a question, it does not act on the
   * order. Its only two controls are a way out to the order page and a way to close.
   */
  it('offers no action on the summary beyond leaving it', fakeAsync(() => {
    const element = load();
    element.querySelector<HTMLButtonElement>('.order-ref')!.click();
    settle();

    const dialog = element.querySelector<HTMLDialogElement>('dialog.summary')!;
    const labels = [...dialog.querySelectorAll('button')].map((b) => b.getAttribute('aria-label'));
    expect(labels).toEqual(['Open the full order', 'Close the order summary']);

    dialog.querySelector<HTMLButtonElement>('.summary-close')!.click();
    settle();
    expect(dialog.open).toBe(false);
  }));

  /* The escalation: the summary answers the small question, the order page answers the rest. */
  it('routes to the order page from the summary, closing it on the way', fakeAsync(() => {
    const element = load();
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    element.querySelector<HTMLButtonElement>('.order-ref')!.click();
    settle();
    element.querySelector<HTMLDialogElement>('dialog.summary')!
      .querySelector<HTMLButtonElement>('.summary-open')!
      .click();
    settle();

    expect(router.navigate).toHaveBeenCalledWith(['/orders', 10481]);
    expect(element.querySelector<HTMLDialogElement>('dialog.summary')!.open).toBe(false);
  }));

  /*
   * `requestRef` is an order id only by a checkout convention, so a reference that does not parse is
   * shown rather than linked — a link there would go to an order that does not exist.
   */
  it('shows a non-numeric reference as plain text with no link', fakeAsync(() => {
    api.ledger = [row(1, 'PENDING', 'MANUAL_TRANSFER', 'sub_1P9xyz')];
    const element = load();

    expect(element.querySelector('.order-ref')).toBeNull();
    expect(element.querySelector('.order-ref-plain')!.textContent!.trim()).toBe('sub_1P9xyz');
  }));

  /* --------------------------------------------------------------------- the writes ---- */

  /*
   * Row 1 is a pending manual transfer; row 2 is a pending *Stripe* payment. Only the first is
   * waiting on a person, and only the first may be approved — see `isApprovable`.
   */
  it('offers approve and reject on a manual transfer, and not on a pending card payment', fakeAsync(() => {
    const element = load();
    clickTab(element, 'All');

    const rows = [...element.querySelectorAll('app-table-row')];
    expect(rows[0].querySelectorAll('.cell-actions button').length).toBe(2);
    expect(rows[1].querySelectorAll('.cell-actions button').length).toBe(0);
    // And nothing settled is actionable either.
    expect(rows[2].querySelectorAll('.cell-actions button').length).toBe(0);
  }));

  /*
   * `approve` is keyed on the UUID, not on the numeric id — the mistake having both invites — and
   * it carries the external transaction number the operator typed.
   */
  it('approves against the internal ref with the number the operator typed', fakeAsync(() => {
    const element = load();

    element.querySelector<HTMLButtonElement>('.cell-actions button')!.click();
    settle();

    const dialog = element.querySelector<HTMLDialogElement>('dialog.approve')!;
    expect(dialog.open).toBe(true);

    const input = dialog.querySelector<HTMLInputElement>('input')!;
    input.value = 'ACME-2291';
    input.dispatchEvent(new Event('input'));
    settle();

    dialog.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    settle();

    expect(api.approvals).toEqual([{ref: 'ref-1', transactionNo: 'ACME-2291'}]);
  }));

  /* `transactionNo` is `@NotBlank` server-side, so an empty box must not reach it. */
  it('refuses to approve without a transaction number', fakeAsync(() => {
    const element = load();

    element.querySelector<HTMLButtonElement>('.cell-actions button')!.click();
    settle();

    const dialog = element.querySelector<HTMLDialogElement>('dialog.approve')!;
    dialog.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    settle();

    expect(api.approvals).toEqual([]);
    expect(dialog.textContent).toContain('Enter the external transaction number.');
  }));

  /* seller-ui rejected on a single click. Rejecting is irreversible and now asks first. */
  it('confirms before rejecting, and says the order will not move', fakeAsync(() => {
    const element = load();

    element.querySelectorAll<HTMLButtonElement>('.cell-actions button')[1].click();
    settle();

    const dialog = element.querySelector<HTMLDialogElement>('dialog.confirm')!;
    expect(dialog.open).toBe(true);
    expect(dialog.textContent).toContain('cancels the order and releases its stock');
    expect(api.rejections).toEqual([]);

    dialog.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    settle();

    expect(api.rejections).toEqual(['ref-1']);
  }));

  /* The endpoints answer `void`, so the page re-reads rather than assuming what the row became. */
  it('re-reads the ledger and the counts after a write', fakeAsync(() => {
    const element = load();
    const listBefore = api.requests.length;
    const countsBefore = api.countCalls;

    element.querySelectorAll<HTMLButtonElement>('.cell-actions button')[1].click();
    settle();
    element
      .querySelector<HTMLDialogElement>('dialog.confirm')!
      .querySelector<HTMLButtonElement>('button[type="submit"]')!
      .click();
    settle();

    expect(api.requests.length).toBeGreaterThan(listBefore);
    expect(api.countCalls).toBeGreaterThan(countsBefore);
  }));

  /* ------------------------------------------------------------------- empty & error ---- */

  it('offers a way back when a filter empties the table, and none when the period is simply empty', fakeAsync(() => {
    api.ledger = [];
    const element = load();
    clickTab(element, 'All');

    // Nothing taken in the period: there is nothing for the operator to undo.
    expect(element.textContent).toContain('No payments were taken in this period.');
    expect(element.querySelector('app-empty-state button')).toBeNull();

    clickTab(element, 'Failed');
    expect(element.textContent).toContain('No transaction matches these filters.');
    expect(element.querySelector('app-empty-state button')).not.toBeNull();
  }));

  it('reports a failed load and retries on demand', fakeAsync(() => {
    api.failure = true;
    const element = load();

    expect(element.querySelector('app-load-error')).not.toBeNull();

    api.failure = false;
    element.querySelector<HTMLButtonElement>('app-load-error button')!.click();
    settle();

    expect(element.querySelector('app-load-error')).toBeNull();
    expect(references(element)).toEqual(['ref-1']);
  }));

  /*
   * Both the queue tab and the gateway select write `paymentType`. Letting the select apply on the
   * queue would silently redefine what the queue means, so it is disabled there instead.
   */
  it('disables the gateway filter on the approval queue', fakeAsync(() => {
    const element = load();
    const select = element.querySelector('app-select')!;
    expect(select.classList).toContain('select-disabled');

    clickTab(element, 'All');
    expect(element.querySelector('app-select')!.classList).not.toContain('select-disabled');
  }));
});
