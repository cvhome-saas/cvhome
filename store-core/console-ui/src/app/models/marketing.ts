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

export interface MarketingPlan {
  readonly nameKey: string;
  readonly monthlyPrice: number;
  readonly descriptionKey: string;
  readonly actionKey: string;
  readonly featured?: boolean;
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
