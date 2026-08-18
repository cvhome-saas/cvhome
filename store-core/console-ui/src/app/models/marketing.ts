import type {BillingInterval, EntitlementKey} from '@models/billing';

export interface MarketingMetric {
  /** A number or symbol, not prose — "60s", "99.95%" — so it is not translated. */
  readonly value: string;
  readonly labelKey: string;
}

export interface MarketingPillar {
  readonly titleKey: string;
  readonly copyKey: string;
}

export interface MarketingStore {
  /** A brand name — proper nouns are not translated. */
  readonly name: string;
  readonly tradeKey: string;
  /** A city name — proper nouns are not translated. */
  readonly market: string;
  readonly mark: string;
  readonly tone: string;
  readonly since: number;
}

export interface MarketingReview {
  /** A person's name — proper nouns are not translated. */
  readonly name: string;
  /** Role and company, as one pre-composed line — the key belongs to the whole record, the
   *  way a real CMS would return one already-localized field rather than parts to join. */
  readonly roleKey: string;
  readonly marketKey: string;
  readonly initials: string;
  readonly quoteKey: string;
  /** Whole stars out of five, as shown in the review card. */
  readonly rating: number;
}

export interface MarketingStat {
  readonly value: string;
  readonly labelKey: string;
}

export interface MarketingChannel {
  readonly icon: 'envelope' | 'messageCircle' | 'phone';
  readonly titleKey: string;
  readonly detailKey: string;
  /** An address or phone number — not translated. */
  readonly value: string;
  readonly href?: string;
}

/**
 * A ceiling or capability a plan grants, ready for the pricing card to label.
 *
 * `limit` is null for a capability (`CUSTOM_DOMAIN`) and for a ceiling the catalog leaves off, which it defines as
 * unlimited. The two read differently on the card, so the distinction is carried rather than flattened to a number.
 */
export interface PlanFeature {
  readonly key: EntitlementKey;
  readonly limit: number | null;
  readonly unlimited: boolean;
}

/** A catalog plan as the pricing section renders it. Everything here comes from billing; nothing is authored. */
export interface PricingPlan {
  /** The `plan_price` id — what a checkout is opened against, not the plan id. */
  readonly priceId: string;
  /** The stable handle (`FREE`, `BASIC`, `PRO`). Used for tracking and tests, never shown. */
  readonly code: string;
  readonly name: string;
  readonly description: string | null;
  /** Major units — `minorUnits / 100`, the way every price on this page is shown. */
  readonly amount: number;
  readonly currency: string;
  readonly interval: BillingInterval;
  readonly trialDays: number;
  readonly features: readonly PlanFeature[];
  readonly free: boolean;
  /**
   * Presentation only. The billing catalog has no "recommended plan" flag, so the console highlights the middle
   * paid tier — see lessons.md, "Marketing — no recommended-plan flag in the billing catalog".
   */
  readonly featured: boolean;
}

/** A stable id for a contact topic — never the display string, which the topic switches. */
export type ContactTopicId = 'migratingStores' | 'newMarketSetup' | 'customPlan' | 'somethingElse';

export interface ContactTopic {
  readonly id: ContactTopicId;
  readonly labelKey: string;
}

export interface ContactRequest {
  readonly name: string;
  readonly organization: string;
  readonly email: string;
  readonly topic: ContactTopicId;
  readonly message: string;
}
