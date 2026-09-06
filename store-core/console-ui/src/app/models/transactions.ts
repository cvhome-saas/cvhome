import type {KpiDatum, PageT, Tone} from '@cvhome-saas/ui-kit';
import type {PaymentStatus} from '@models/payment';

/**
 * The payments ledger's view models.
 *
 * The wire shapes live in `@models/payment`; this is what the page renders — the `checkout.ts` /
 * `orders.ts` split, applied again.
 *
 * What is **not** here is the interesting part. `console-template/Payments.dc.html` designs a
 * customer column, a card brand and last four, a per-row gateway fee, a payout schedule, a dispute
 * count and a volume series. A transaction carries none of them and no endpoint computes any of
 * them, so none of them has a field here. See lessons.md, the Payments entries.
 */

/** Every `PaymentStatus`, in the order the server's enum declares them — which is the tab order. */
export const PAYMENT_STATUS_VALUES: readonly PaymentStatus[] = [
  'PENDING',
  'PROCESSING',
  'PAID',
  'FAILED',
  'EXPIRED',
  'CANCELLED',
  'WAITING_VERIFICATION',
  'REJECTED',
  'AUTHORIZED',
  'REFUNDED',
];

/**
 * Status to its categorical tone.
 *
 * Grouped by meaning, and deliberately consistent with `@models/orders`' `STATUS_TONE`: the four
 * values the two enums share — `PROCESSING`, `CANCELLED`, and the terminal-good and terminal-bad
 * families — read the same colour on the ledger as they do in an order's payment badge.
 */
export const TRANSACTION_TONE: Readonly<Record<PaymentStatus, Tone>> = {
  PENDING: 'amber',
  WAITING_VERIFICATION: 'amber',
  PROCESSING: 'blue',
  AUTHORIZED: 'cyan',
  PAID: 'green',
  FAILED: 'red',
  REJECTED: 'red',
  CANCELLED: 'slate',
  EXPIRED: 'slate',
  REFUNDED: 'violet',
};

/**
 * The tab strip: the approval queue, then everything, then each real status.
 *
 * Thirteen is more than the mockup's six, and the strip scrolls — the same deviation Module 4 made
 * for orders, for the same reason. Grouping them would mean inventing groups the API cannot filter
 * by, since `status` takes exactly one value.
 *
 * `queue` is the one compound tab: `status=PENDING` **and** `paymentType=MANUAL_TRANSFER`, which the
 * server ANDs. It leads because it is the only tab an operator has to *do* something about.
 */
export type PaymentTab = 'queue' | 'all' | PaymentStatus;

export const PAYMENT_TABS: readonly PaymentTab[] = ['queue', 'all', ...PAYMENT_STATUS_VALUES];

/**
 * How a payments row addresses its order: by numeric id for the transactions the retired checkout
 * wrote (`requestRef` = order id), by opaque ref for everything the rewritten checkout placed.
 * Exactly one of the two is set.
 */
export interface OrderKey {
  readonly id: number | null;
  readonly ref: string | null;
}

/** One row of the ledger. Every field is read off the transaction the list already returned. */
export interface TransactionRow {
  /** `ReadableTransaction.id`, the track-by. Never the key for approve or reject. */
  readonly id: number;
  /** The UUID. What `approve` and `reject` are addressed by, and what the operator copies. */
  readonly internalRef: string;
  /** The external reference, set only once a payment is approved or settled. */
  readonly transactionNo: string | null;
  /**
   * The order id this transaction paid for, or null when `requestRef` does not read as one.
   *
   * Null is not hypothetical: `requestRef` is only an order id by convention, so anything that is
   * not a positive integer is treated as an opaque reference and not offered as a link.
   */
  readonly orderId: number | null;
  /**
   * The order ref this transaction paid for, or null when `requestRef` does not read as one.
   *
   * Checkout hands payment its opaque `orderRef` (a UUID), so this is the link for every order the
   * rewritten checkout placed; `orderId` survives for the transactions the retired service wrote.
   */
  readonly orderRef: string | null;
  /** `requestRef` verbatim, for the rows where it is neither an order id nor an order ref. */
  readonly reference: string;
  /** The server's `PaymentType` name, unlabelled — the page translates it through the known-set guard. */
  readonly paymentType: string;
  readonly status: PaymentStatus;
  /**
   * The amount and its currency, not a rendered string: the row survives a language change, and the
   * page formats it in the language the operator is actually reading.
   */
  readonly amount: {readonly value: number | null; readonly currency: string | null};
  readonly placedOn: string;
  /** Whether Approve and Reject are offered — `ACTIONABLE_STATUSES` membership. */
  readonly actionable: boolean;
}

/** One ledger KPI's source data, resolved into a `KpiDatum` by the facade. */
export interface TransactionKpiSource {
  readonly labelKey: string;
  /** Null when the count could not be read — an em dash under a flag, never a zero. */
  readonly value: string | null;
  readonly icon: KpiDatum['icon'];
  readonly tone: KpiDatum['tone'];
  readonly flagKey?: string;
}

/** Everything the ledger renders for one query. */
export interface TransactionsSnapshot {
  readonly page: PageT<TransactionRow>;
}

/** One line of the order, as the summary dialog prints it. */
export interface OrderSummaryLine {
  readonly id: number;
  readonly name: string;
  readonly quantity: number;
  readonly lineTotal: string;
}

/**
 * An order, small enough to read without leaving the ledger.
 *
 * Deliberately a *summary* and not the detail page in a box: no status composer, no invoice, no
 * address editing, no payment panel — the operator is already looking at the payment. It answers
 * "what did this pay for", which is the question the order number in a transaction row raises.
 */
export interface OrderSummary {
  readonly id: number;
  readonly reference: string;
  readonly status: string;
  readonly tone: Tone;
  readonly placedOn: string | null;
  readonly customer: string;
  readonly email: string;
  readonly lines: readonly OrderSummaryLine[];
  readonly itemCount: number;
  readonly total: string;
}
