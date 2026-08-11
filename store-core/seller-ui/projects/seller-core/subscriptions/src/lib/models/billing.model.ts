/**
 * The billing service's wire shapes.
 *
 * Identifiers arrive wrapped — `{"id": "..."}` — because the server models them as value objects rather than bare
 * strings, and every store-core DTO serialises them that way. They are passed back unchanged rather than unwrapped
 * and rebuilt, so a change to that representation is a compile error here instead of a silent mismatch.
 */
export interface Identifier {
  id: string;
}

export interface Money {
  currency: { code: string };
  minorUnits: number;
}

/** A ceiling (`limitValue`) or a capability (`flagValue`); never both. Both absent means unlimited. */
export interface EntitlementValue {
  key: string;
  limitValue: number | null;
  flagValue: boolean | null;
}

export type Entitlements = Record<string, EntitlementValue>;

export type SubscriptionStatus =
  | 'PENDING'
  | 'TRIALING'
  | 'ACTIVE'
  | 'PAST_DUE'
  | 'SUSPENDED'
  | 'CANCELED';

export type BillingInterval = 'MONTH' | 'YEAR';

export interface PlanPriceView {
  id: Identifier;
  amount: Money;
  interval: BillingInterval;
  trialDays: number;
}

export interface PlanView {
  id: Identifier;
  code: string;
  displayName: string;
  description: string | null;
  tier: number;
  prices: PlanPriceView[];
  entitlements: Entitlements;
}

/** A downgrade agreed but not yet in force — the customer keeps the plan they paid for until `effectiveAt`. */
export interface PendingPlanChangeView {
  planPriceId: Identifier;
  planCode: string | null;
  effectiveAt: string;
}

export interface SubscriptionView {
  store: Identifier;
  status: SubscriptionStatus;
  planCode: string | null;
  planDisplayName: string | null;
  planPriceId: Identifier | null;
  amount: Money | null;
  /** The next renewal date, or when access ends once renewal has been switched off. */
  currentPeriodEnd: string | null;
  trialEnd: string | null;
  cancelAtPeriodEnd: boolean;
  /** How long a failed renewal has left before the store is suspended. */
  graceUntil: string | null;
  pendingPlanChange: PendingPlanChangeView | null;
  providerLinked: boolean;
  entitlements: Entitlements;
}

export type InvoiceStatus = 'DRAFT' | 'OPEN' | 'PAID' | 'UNCOLLECTIBLE' | 'VOID';

export interface InvoiceView {
  id: Identifier;
  number: string | null;
  status: InvoiceStatus;
  amountDue: Money;
  amountPaid: Money;
  periodStart: string | null;
  periodEnd: string | null;
  issuedAt: string;
  paidAt: string | null;
  /** Stripe's own documents. Shown rather than re-rendered, so what the customer files matches what was issued. */
  hostedInvoiceUrl: string | null;
  invoicePdfUrl: string | null;
}

export interface CheckoutSessionView {
  url: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
}

/** Whether the store can be worked in. Mirrors `SubscriptionStatus.operable()` on the server. */
export function isOperable(status: SubscriptionStatus): boolean {
  return status === 'TRIALING' || status === 'ACTIVE' || status === 'PAST_DUE';
}

/** Formats minor units for display — the server speaks cents so nothing rounds on the way in. */
export function formatAmount(amount: Money | null): string {
  if (!amount) {
    return '';
  }
  return (amount.minorUnits / 100).toFixed(2);
}
