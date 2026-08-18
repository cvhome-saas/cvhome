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
