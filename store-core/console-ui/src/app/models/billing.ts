/**
 * Ported from seller-ui/projects/seller-core/subscriptions/src/lib/models/billing.model.ts.
 *
 * The billing service's wire shapes.
 *
 * Identifiers arrive wrapped — `{"id": "..."}` — because the server models them as value objects rather than bare
 * strings, and every store-core DTO serialises them that way. They are passed back unchanged rather than unwrapped
 * and rebuilt, so a change to that representation is a compile error here instead of a silent mismatch.
 */
export interface Identifier {
  readonly id: string;
}

export interface Money {
  readonly currency: {readonly code: string};
  readonly minorUnits: number;
}

/**
 * What a plan grants, as `billing.commons.EntitlementKey` declares it.
 *
 * Fixed and small by design — the plan catalog is the product's vocabulary, so the console can render every key
 * rather than falling back to printing raw identifiers the way the old pricing page did.
 */
export type EntitlementKey =
  | 'MAX_PRODUCTS'
  | 'MAX_ORDERS_MONTH'
  | 'MAX_ACCOUNTS'
  | 'MAX_STORAGE_MB'
  | 'CUSTOM_DOMAIN'
  | 'ANALYTICS'
  | 'PRIORITY_SUPPORT';

/** A ceiling (`limitValue`) or a capability (`flagValue`); never both. Both absent means unlimited. */
export interface EntitlementValue {
  readonly key: EntitlementKey;
  readonly limitValue: number | null;
  readonly flagValue: boolean | null;
}

/**
 * An omitted key means unlimited, which is why this is `Partial` rather than a total record: reading a missing key
 * as "not granted" is the exact inversion of what the catalog means by leaving it out.
 */
export type Entitlements = Partial<Record<EntitlementKey, EntitlementValue>>;

export type SubscriptionStatus =
  | 'PENDING'
  | 'TRIALING'
  | 'ACTIVE'
  | 'PAST_DUE'
  | 'SUSPENDED'
  | 'CANCELED';

export type BillingInterval = 'MONTH' | 'YEAR';

export interface PlanPriceView {
  readonly id: Identifier;
  readonly amount: Money;
  readonly interval: BillingInterval;
  readonly trialDays: number;
}

export interface PlanView {
  readonly id: Identifier;
  readonly code: string;
  readonly displayName: string;
  readonly description: string | null;
  readonly tier: number;
  readonly prices: readonly PlanPriceView[];
  readonly entitlements: Entitlements;
}

/* ------------------------------------------------------------------------------------------------
 * A store's own subscription. Everything above describes the *catalogue*; everything below describes
 * what one store is actually on, and is store-scoped on the wire (`?store=`).
 * ---------------------------------------------------------------------------------------------- */

/**
 * A plan change that has been agreed but has not taken effect.
 *
 * Only downgrades land here. An upgrade is applied immediately and comes back as the new plan; a
 * downgrade waits for the period to end, so the store keeps what it paid for. Mirrors
 * `PendingPlanChangeView`.
 */
export interface PendingPlanChange {
  readonly planPriceId: string;
  readonly planCode: string;
  /** ISO-8601 instant. */
  readonly effectiveAt: string;
}

/**
 * Mirrors `billing.commons.dto.SubscriptionView` — what `GET subscription/current?store=` answers.
 *
 * Instants are ISO-8601 strings, not `Date`: they arrive as strings and are formatted for display,
 * and parsing them into `Date` at the edge only invites a timezone bug in the middle.
 *
 * `entitlements` is the same map the catalogue uses, so the *granted* ceilings can be compared
 * against the *plan's* without a second shape.
 */
export interface Subscription {
  readonly store: {readonly id: string} | string;
  readonly status: SubscriptionStatus;
  readonly planCode: string | null;
  readonly planDisplayName: string | null;
  readonly planPriceId: string | null;
  readonly amount: Money | null;
  /** When the current period ends — the renewal date, or the cut-off if cancelling. */
  readonly currentPeriodEnd: string | null;
  readonly trialEnd: string | null;
  /** Renewal is off: the store keeps its plan until `currentPeriodEnd` and then stops. */
  readonly cancelAtPeriodEnd: boolean;
  /** How long a past-due store stays operable before enforcement bites. Null when not past due. */
  readonly graceUntil: string | null;
  readonly pendingPlanChange: PendingPlanChange | null;
  /** Whether a payment provider customer exists. False for a store that has never checked out. */
  readonly providerLinked: boolean;
  readonly entitlements: Entitlements;
}

/** What `POST subscription/checkout` answers: where to send the browser. */
export interface CheckoutSession {
  readonly url: string;
}

export type InvoiceStatus = 'DRAFT' | 'OPEN' | 'PAID' | 'UNCOLLECTIBLE' | 'VOID';

/**
 * Mirrors `InvoiceView`.
 *
 * `hostedInvoiceUrl` and `invoicePdfUrl` are Stripe's own pages. The console links to them rather
 * than rendering an invoice itself — the provider's copy is the one that is legally the invoice.
 */
export interface Invoice {
  readonly id: string;
  readonly number: string | null;
  readonly status: InvoiceStatus;
  readonly amountDue: Money;
  readonly amountPaid: Money;
  readonly periodStart: string | null;
  readonly periodEnd: string | null;
  readonly issuedAt: string | null;
  readonly paidAt: string | null;
  readonly hostedInvoiceUrl: string | null;
  readonly invoicePdfUrl: string | null;
}
