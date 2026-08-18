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
