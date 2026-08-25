/** Mirrors payment's `PaymentStatus`. */
export type PaymentStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'PAID'
  | 'FAILED'
  | 'EXPIRED'
  | 'CANCELLED'
  | 'WAITING_VERIFICATION'
  | 'REJECTED'
  | 'AUTHORIZED'
  | 'REFUNDED';

/**
 * The status that names the approval queue, and that nothing ever sets.
 *
 * It reads like the answer — a manual bank transfer should sit here until someone confirms the money
 * arrived — and it is a trap. `WAITING_VERIFICATION` appears in exactly two places in the platform's
 * Java: its own declaration, and one line mapping it onto a gateway result. No processor returns it;
 * `ManualTransferredProcessor.initiate` returns `PENDING`. A console filter on it counts zero
 * forever, which is how the dashboard's "payment approvals waiting" tile shipped reading zero.
 *
 * Kept, because a transaction *could* arrive in it and the ledger must render it. Never filtered on:
 * `PENDING_APPROVAL` + `MANUAL_TRANSFER` is the queue. See lessons.md, "Payments — the approval
 * queue's own status is never set".
 */
export const AWAITING_VERIFICATION: PaymentStatus = 'WAITING_VERIFICATION';

/** Where a manual transfer actually waits. Half of the approval queue's filter. */
export const PENDING_APPROVAL: PaymentStatus = 'PENDING';

/** The gateway that needs a person. The other half — a `PENDING` card payment is nobody's queue. */
export const MANUAL_TRANSFER = 'MANUAL_TRANSFER';

/**
 * The gateways where a person, not a processor, decides that the money arrived.
 *
 * `PaymentType.attrs` is empty for exactly these two — they have no credentials because there is no
 * gateway behind them. A bank transfer is confirmed by reading a statement and cash on delivery by
 * the courier handing it over; both are human facts the platform cannot observe.
 *
 * Stripe and PayPal are the opposite: the processor settles them and reports back through the
 * webhook. Nothing about them is waiting on the operator.
 */
export const MANUALLY_SETTLED_TYPES: readonly string[] = ['MANUAL_TRANSFER', 'COD'];

/**
 * The statuses on which Approve and Reject are offered — **for a manually settled gateway only**.
 *
 * seller-ui offered them on these four statuses for *every* gateway, and QA against the live stack
 * showed what that costs: a `PENDING` Stripe payment the gateway had not settled sat there with an
 * Approve button. `approve` has no state guard and no gateway guard — it sets `PAID` and fires
 * `PaymentPaidEvent` whatever it is given — so pressing it would have told checkout an order was
 * paid for which no money had been taken, with no refund endpoint anywhere to undo it.
 *
 * Narrowing this is a deliberate removal from seller-ui's behaviour, and the only guard there is:
 * see `isApprovable`, and lessons.md, "Payments — approve and reject are unguarded and not
 * idempotent".
 */
export const ACTIONABLE_STATUSES: readonly PaymentStatus[] = [
  'PENDING',
  'PROCESSING',
  'WAITING_VERIFICATION',
  'AUTHORIZED',
];

/**
 * Whether an operator may approve or reject this transaction.
 *
 * Both halves matter. The status half keeps a settled payment from being re-approved; the gateway
 * half keeps a card payment from being marked paid by hand. Neither is enforced server-side.
 */
export function isApprovable(status: PaymentStatus, paymentType: string): boolean {
  return ACTIONABLE_STATUSES.includes(status) && MANUALLY_SETTLED_TYPES.includes(paymentType);
}

export interface PaymentTransaction {
  /**
   * The row's stable key.
   *
   * `ReadableTransaction` carries it and seller-core's model dropped it, leaving the old table
   * tracking rows by object identity. Restored here as the track-by. It is **not** the key the write
   * endpoints take — `approve` and `reject` are addressed by `internalRef`.
   */
  readonly id: number;
  readonly internalRef: string;
  /**
   * The order this paid for, as a string — but only by convention.
   *
   * `OrderPlacementFacadeImpl` writes `modelOrder.getId().toString()` into the payment request's
   * `ref`, which lands here. Nothing types it, nothing enforces it, and the payment service has no
   * idea it is an order; `payment.transaction` even carries a dead, unmapped `order_id` column. The
   * console traverses it anyway, because it is the only link there is. See lessons.md, "Payments —
   * the link from a transaction to its order is a convention".
   */
  readonly requestRef: string;
  readonly amount: number;
  readonly currency: {readonly code: string};
  readonly paymentType: string;
  readonly transactionDate: string;
  readonly status: PaymentStatus;
  readonly transactionNo: string | null;
}

/** What `POST …/transaction/{internalRef}/approve` takes. `transactionNo` is `@NotBlank`. */
export interface PaymentApproval {
  readonly transactionNo: string;
}

/**
 * The filter `TransactionSearchFilter` accepts, as query parameters.
 *
 * The dates are `Instant`s server-side, so they bind from ISO-8601 with a zone
 * (`2026-08-01T00:00:00Z`) and not from `yyyy-MM-dd`. Omit a key rather than sending an empty
 * string — see `PaymentService.transactions`.
 */
export interface TransactionQuery {
  readonly status?: PaymentStatus;
  readonly paymentType?: string;
  readonly requestRef?: string;
  readonly internalRef?: string;
  readonly transactionDateFrom?: string;
  readonly transactionDateTo?: string;
}

/*
 * Payment configuration — the store's gateway credentials, as
 * `payment-core/models/{Readable,Persistable}PaymentConfiguration` declare them.
 */

/**
 * A gateway's stored credentials, **in cleartext**.
 *
 * They are encrypted at rest and `PaymentConfigurationMapper.toDTO` decrypts all three before
 * serialising, so a `GET` hands the browser the live Stripe secret key and webhook secret. The
 * console does not pretend otherwise: it shows them behind a reveal toggle, because that is what
 * the endpoint returns and an operator needs to be able to check a key. See lessons.md, "Store
 * management — payment and social-login reads return secrets in cleartext".
 *
 * A field reads `null` both when nothing is stored **and** when what is stored was written before
 * encryption existed — `decrypt()` returns `null` for anything not in encrypted form, so the two
 * are indistinguishable from here.
 */
export interface ReadablePaymentConfiguration {
  readonly paymentType: string;
  readonly apiKey: string | null;
  readonly secretKey: string | null;
  readonly webhookSecret: string | null;
  readonly enabled: boolean;
}

/**
 * What `POST /private/payment-configuration` and `PUT …/{paymentType}` take.
 *
 * The two differ in how they treat an absent field, and it matters. `POST` builds a fresh entity, so
 * anything omitted is written as null. `PUT` goes through `updateEntity`, which skips a `null` field
 * and leaves the stored value alone — so omitting a secret on an update is how you keep it.
 */
export interface PersistablePaymentConfiguration {
  readonly paymentType: string;
  readonly apiKey?: string | null;
  readonly secretKey?: string | null;
  readonly webhookSecret?: string | null;
  readonly enabled: boolean;
}
