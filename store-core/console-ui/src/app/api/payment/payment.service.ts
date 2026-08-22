/** Ported from seller-ui/projects/seller-core/payments/src/lib/services/payment.service.ts. */
import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {PageT} from '@core/table/table.types';
import {
  MANUAL_TRANSFER,
  PENDING_APPROVAL,
  type PaymentApproval,
  type PaymentStatus,
  type PaymentTransaction,
  type TransactionQuery,
} from '@models/payment';

/**
 * Ported from seller-ui/projects/seller-core/payments/src/lib/services/payment.service.ts, verified
 * against `PrivatePaymentApi`.
 *
 * The whole seller-facing transaction surface, and it is three endpoints: a filtered paged list,
 * approve, reject. There is no detail endpoint — `GET …/transactions/{internalRef}` does not exist,
 * so the row the list returns is all there is. See lessons.md, "Payments — no transaction detail
 * endpoint".
 *
 * The gateway *credentials* behind these transactions are a different service —
 * `@api/payment/payment-configuration.service.ts`.
 */
const PAYMENT_API_BASE = '/spg/payment/api/v1/private/payment';

@Injectable({providedIn: 'root'})
export class PaymentService {
  private readonly crudService = inject(CrudService);

  /**
   * A page of transactions.
   *
   * `count`, not Spring's `size`: `store-commons/autoconfigure`'s `ServletWebConfig` registers a
   * `PageableHandlerMethodArgumentResolver` with `setSizeParameterName("count")` and the payment
   * service depends on that module, so the platform-wide name reaches this bare `Pageable` too.
   *
   * A key whose value is `undefined` never reaches the wire — `CrudService.getParams` drops it. That
   * is deliberate and is why the callers omit a cleared filter rather than sending `''`, which is
   * what seller-ui does and which only works because Spring happens to bind an empty string to null.
   */
  transactions(query: TransactionQuery & {page: number; count: number}): Observable<PageT<PaymentTransaction>> {
    return this.crudService.get(`${PAYMENT_API_BASE}/transactions`, {...query});
  }

  /**
   * How many transactions sit in one status.
   *
   * Asks for a single row and reads `totalElements`, because there is no count endpoint — the only
   * way to learn how many of something there are is to fetch some of them. Cheap at one row, but see
   * lessons.md, "Dashboard — counting requires fetching".
   */
  countByStatus(status: PaymentStatus, range?: TransactionQuery): Observable<number> {
    return this.count({...range, status});
  }

  /**
   * How many payments are waiting on a person.
   *
   * **Not `WAITING_VERIFICATION`.** That status names this queue and nothing ever sets it —
   * `ManualTransferredProcessor.initiate` returns `PENDING` — so a filter on it counts zero forever.
   * A manual bank transfer in `PENDING` is what an operator actually has to confirm, and pairing the
   * status with the payment type is what separates it from a card payment the gateway has not
   * settled yet. See lessons.md, "Payments — the approval queue's own status is never set".
   */
  countAwaitingApproval(range?: TransactionQuery): Observable<number> {
    return this.count({...range, status: PENDING_APPROVAL, paymentType: MANUAL_TRANSFER});
  }

  /**
   * Confirms a payment against an external reference — the bank's transaction number for a transfer
   * the operator has seen arrive.
   *
   * Keyed on `internalRef`, the UUID, **not** on `ReadableTransaction.id`. Sets `PAID` and fires
   * `PaymentPaidEvent`, which is what moves the order. The server checks nothing first: there is no
   * guard that the transaction is in an approvable state and no idempotency, so approving one that
   * is already `PAID` re-fires the event. The console is what keeps that from happening — see
   * lessons.md, "Payments — approve and reject are unguarded and not idempotent".
   */
  approve(internalRef: string, approval: PaymentApproval): Observable<void> {
    return this.crudService.post(`${PAYMENT_API_BASE}/transaction/${internalRef}/approve`, approval);
  }

  /**
   * Refuses a payment.
   *
   * Sets `REJECTED` and fires **nothing** — checkout is never told, so the order stays exactly where
   * it was. See lessons.md, "Payments — rejecting a payment tells checkout nothing".
   */
  reject(internalRef: string): Observable<void> {
    return this.crudService.post(`${PAYMENT_API_BASE}/transaction/${internalRef}/reject`, {});
  }

  private count(query: TransactionQuery): Observable<number> {
    return this.transactions({...query, page: 0, count: 1}).pipe(map((page) => page.totalElements));
  }
}
