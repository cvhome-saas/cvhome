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
 * The one status a merchant has to act on.
 *
 * A manual bank transfer sits here until someone confirms the money arrived — it is the queue the
 * dashboard's "payment approvals waiting" row counts. seller-ui treats four statuses as actionable
 * (`PENDING, PROCESSING, WAITING_VERIFICATION, AUTHORIZED`), but that list decides which table rows
 * show Approve/Reject buttons; only this one is genuinely *waiting on a person*.
 */
export const AWAITING_VERIFICATION: PaymentStatus = 'WAITING_VERIFICATION';

export interface PaymentTransaction {
  readonly internalRef: string;
  readonly requestRef: string;
  readonly amount: number;
  readonly currency: {readonly code: string};
  readonly paymentType: string;
  readonly transactionDate: string;
  readonly status: PaymentStatus;
  readonly transactionNo: string | null;
}

/** The filter `TransactionSearchFilter` accepts, as query parameters. */
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
