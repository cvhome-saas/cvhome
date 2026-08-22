/**
 * Ported from seller-ui/projects/seller-core/payments/src/lib/models/payment-transaction.model.ts,
 * with the status enum added from `payment-core`'s `PaymentStatus`.
 */

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
 * The statuses that get Approve and Reject buttons.
 *
 * seller-ui's list, carried over unchanged. It is wider than the approval queue on purpose: the
 * queue is what the console *surfaces*, this is what it permits an operator to act on when they have
 * found the row some other way. The server permits far more — `approve` has no state guard at all —
 * so this set is the only thing standing between an operator and re-firing `PaymentPaidEvent` on a
 * transaction that is already paid.
 */
export const ACTIONABLE_STATUSES: readonly PaymentStatus[] = [
  'PENDING',
  'PROCESSING',
  'WAITING_VERIFICATION',
  'AUTHORIZED',
];

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
