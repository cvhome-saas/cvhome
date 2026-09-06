import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map} from 'rxjs';

import {OrdersService} from '@api/orders/orders.service';
import {PaymentService} from '@api/payment/payment.service';
import {optionalOne} from '@cvhome-saas/ui-kit';
import type {PageRequest, PageT} from '@cvhome-saas/ui-kit';
import {
  MANUAL_TRANSFER,
  PENDING_APPROVAL,
  isApprovable,
  type PaymentApproval,
  type PaymentTransaction,
  type TransactionQuery,
} from '@models/payment';
import type {ReadableOrder} from '@models/checkout';
import type {OrderKey, PaymentTab, TransactionRow, TransactionsSnapshot} from '@models/transactions';
import type {DateRangeValue} from '@cvhome-saas/ui-kit/ui';

/** The KPI row's four figures. `null` is "could not be read", which is not the same as zero. */
export interface TransactionCounts {
  readonly queue: number | null;
  readonly paid: number | null;
  readonly failed: number | null;
  readonly refunded: number | null;
}

/** What the page asks for: a tab, a gateway, a search term, a period and a page. */
export interface PaymentsQuery {
  readonly tab: PaymentTab;
  /** A `PaymentType` name, or `''` for every gateway. */
  readonly gateway: string;
  readonly search: string;
  readonly page: PageRequest;
  readonly range: DateRangeValue;
}

/**
 * The payments ledger's data.
 *
 * One leg, and that is the whole story of this module. There is **no aggregate endpoint anywhere in
 * the payment service** — no counts, no sums, no group-by, no series — so unlike the orders page
 * there is no statistic call to fold in beside the list. The KPI counts the page shows are four
 * separate one-row reads of this same endpoint, and they are loaded on their own key by the facade
 * so that changing a tab does not re-ask four questions whose answers did not change.
 *
 * See lessons.md, "Payments — nothing aggregates a transaction".
 */
@Injectable({providedIn: 'root'})
export class PaymentsApi {
  private readonly payments = inject(PaymentService);
  private readonly orders = inject(OrdersService);

  /**
   * The four KPI counts over a period.
   *
   * Four requests for four numbers, because the payment service has no count endpoint and no
   * aggregate of any kind: each is a one-row fetch read for its `totalElements`. Every leg is
   * `optionalOne`, so a figure that cannot be read renders as an em dash rather than as a zero —
   * on a page whose point is an approval queue, "nothing is waiting" is the most dangerous wrong
   * answer available. See lessons.md, "Payments — nothing aggregates a transaction" and
   * "Dashboard — counting requires fetching".
   *
   * Loaded on its own key by the facade — the store and the period, never the tab — so changing a
   * tab does not re-ask four questions whose answers did not move.
   */
  loadCounts(range: DateRangeValue): Observable<TransactionCounts> {
    const period = rangeParams(range);
    return forkJoin({
      queue: this.payments.countAwaitingApproval(period).pipe(optionalOne()),
      paid: this.payments.countByStatus('PAID', period).pipe(optionalOne()),
      failed: this.payments.countByStatus('FAILED', period).pipe(optionalOne()),
      refunded: this.payments.countByStatus('REFUNDED', period).pipe(optionalOne()),
    });
  }

  loadSnapshot(query: PaymentsQuery): Observable<TransactionsSnapshot> {
    return this.payments
      .transactions({...toTransactionQuery(query), page: query.page.page, count: query.page.count})
      .pipe(map((page): TransactionsSnapshot => ({page: {...page, content: page.content.map(toRow)}})));
  }

  /**
   * The order a transaction paid for, for the summary dialog.
   *
   * Returned as the wire shape rather than a formatted view model: the dialog's money, dates and
   * status label all have to re-read when the operator switches language, and a string formatted
   * here would keep the language it was mapped in. The facade shapes it inside a `computed`.
   *
   * The detail endpoint, not the list: only it populates `products` and `customer`.
   */
  loadOrder(key: OrderKey): Observable<ReadableOrder> {
    return key.id !== null ? this.orders.get(key.id) : this.orders.byRef(key.ref ?? '');
  }

  approve(internalRef: string, approval: PaymentApproval): Observable<void> {
    return this.payments.approve(internalRef, approval);
  }

  reject(internalRef: string): Observable<void> {
    return this.payments.reject(internalRef);
  }
}

/* --------------------------------------------------------------------------- shaping ---- */

/**
 * The page's filter, as the endpoint's parameters.
 *
 * Three things worth naming:
 *
 * **The queue tab is a compound.** `queue` is `status=PENDING` **and**
 * `paymentType=MANUAL_TRANSFER`, which `TransactionSearchFilter` ANDs. It is not
 * `WAITING_VERIFICATION`, which nothing ever sets — see `@models/payment`.
 *
 * **The gateway filter loses to the queue tab.** Both write `paymentType`, and the queue's meaning
 * depends on its own value, so selecting a gateway while the queue is open would silently turn the
 * queue into something else. The page disables the gateway select on that tab rather than letting
 * one filter quietly overwrite another.
 *
 * **One search box, two server fields.** `requestRef` is an order id and `internalRef` is a UUID, so
 * the term goes to exactly one of them by shape. Sending it as both would AND to nothing.
 */
export function toTransactionQuery(query: PaymentsQuery): TransactionQuery {
  const term = query.search.trim();
  const queue = query.tab === 'queue';

  return {
    ...(queue ? {status: PENDING_APPROVAL, paymentType: MANUAL_TRANSFER} : {}),
    ...(!queue && query.tab !== 'all' ? {status: query.tab} : {}),
    ...(!queue && query.gateway ? {paymentType: query.gateway} : {}),
    ...(term ? (/^\d+$/.test(term) ? {requestRef: term} : {internalRef: term}) : {}),
    ...rangeParams(query.range),
  };
}

/**
 * A period, as the two `Instant`s the filter binds from.
 *
 * Whole local days widened to their instants, so "today" means the operator's today and not UTC's.
 * `yyyy-MM-dd` would not bind at all — the server's fields are `Instant`, not `LocalDate`.
 */
export function rangeParams(range: DateRangeValue): TransactionQuery {
  const {from, to} = range;
  return {
    ...(from ? {transactionDateFrom: startOfDay(from).toISOString()} : {}),
    ...(to ? {transactionDateTo: endOfDay(to).toISOString()} : {}),
  };
}

/**
 * One transaction, as a table row.
 *
 * The columns the template designs and this cannot fill: the customer (a transaction carries no
 * reference to one), the card brand and last four (they live inside the gateway, not on the row),
 * and the per-row fee (no fee field exists anywhere in payment or checkout). See lessons.md.
 *
 * `actionable` reads the gateway as well as the status. A `PENDING` Stripe payment is not waiting on
 * a person — the processor has it — and offering Approve there would let an operator tell checkout
 * an order was paid that was not. See `isApprovable`.
 */
function toRow(transaction: PaymentTransaction): TransactionRow {
  return {
    id: transaction.id,
    internalRef: transaction.internalRef,
    transactionNo: transaction.transactionNo,
    orderId: asOrderId(transaction.requestRef),
    orderRef: asOrderRef(transaction.requestRef),
    reference: transaction.requestRef,
    paymentType: transaction.paymentType,
    status: transaction.status,
    // The amount and its currency, not a rendered string: the row survives a language change.
    amount: {value: transaction.amount ?? null, currency: transaction.currency?.code ?? null},
    placedOn: transaction.transactionDate,
    actionable: isApprovable(transaction.status, transaction.paymentType),
  };
}

/**
 * `requestRef` read as an order id, or null when it does not read as one.
 *
 * Checkout writes the order's numeric id here and nothing enforces that, so anything which is not a
 * positive integer is left as an opaque reference rather than turned into a link that would 404.
 */
function asOrderId(requestRef: string): number | null {
  return /^\d+$/.test(requestRef) && Number(requestRef) > 0 ? Number(requestRef) : null;
}

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

/** `requestRef` read as checkout's opaque order ref, or null when it is not UUID-shaped. */
function asOrderRef(requestRef: string): string | null {
  return UUID.test(requestRef) ? requestRef : null;
}

function startOfDay(date: Date): Date {
  const copy = new Date(date);
  copy.setHours(0, 0, 0, 0);
  return copy;
}

function endOfDay(date: Date): Date {
  const copy = new Date(date);
  copy.setHours(23, 59, 59, 999);
  return copy;
}

export type {PageT};
