import {
  ContactTopic,
  MarketingChannel,
  MarketingMetric,
  MarketingPillar,
  MarketingPlan,
  MarketingReview,
  MarketingStat,
  MarketingStore,
} from '@models/marketing';

export const MARKETING_METRICS: readonly MarketingMetric[] = [
  {value: '60s', labelKey: 'marketing.metric.timeToOpen'},
  {value: '4', labelKey: 'marketing.metric.continents'},
  {value: '4', labelKey: 'marketing.metric.languages'},
  {value: '99.95%', labelKey: 'marketing.metric.uptime'},
];

export const MARKETING_PILLARS: readonly MarketingPillar[] = [
  {titleKey: 'marketing.pillar.openMarket.title', copyKey: 'marketing.pillar.openMarket.copy'},
  {titleKey: 'marketing.pillar.localLanguage.title', copyKey: 'marketing.pillar.localLanguage.copy'},
  {titleKey: 'marketing.pillar.oneSeat.title', copyKey: 'marketing.pillar.oneSeat.copy'},
];

export const MARKETING_STORES: readonly MarketingStore[] = [
  {name: 'Acme Supply Co.', tradeKey: 'marketing.store.acme.trade', market: 'San Francisco', mark: 'A', tone: 'emerald', since: 2024},
  {name: 'Northline Labs', tradeKey: 'marketing.store.northline.trade', market: 'Frankfurt', mark: 'N', tone: 'blue', since: 2025},
  {name: 'Harbor Point', tradeKey: 'marketing.store.harborPoint.trade', market: 'Seattle', mark: 'H', tone: 'amber', since: 2025},
  {name: 'Vantage Interiors', tradeKey: 'marketing.store.vantage.trade', market: 'Bahrain', mark: 'V', tone: 'violet', since: 2026},
];

export const MARKETING_REVIEWS: readonly MarketingReview[] = [
  {
    name: 'Marta Kowalska',
    roleKey: 'marketing.review.marta.role',
    marketKey: 'marketing.review.marta.market',
    initials: 'MK',
    rating: 5,
    quoteKey: 'marketing.review.marta.quote',
  },
  {
    name: 'Tobias Lindqvist',
    roleKey: 'marketing.review.tobias.role',
    marketKey: 'marketing.review.tobias.market',
    initials: 'TL',
    rating: 5,
    quoteKey: 'marketing.review.tobias.quote',
  },
  {
    name: 'Amina Haddad',
    roleKey: 'marketing.review.amina.role',
    marketKey: 'marketing.review.amina.market',
    initials: 'AH',
    rating: 4,
    quoteKey: 'marketing.review.amina.quote',
  },
  {
    name: 'Daniel Okoye',
    roleKey: 'marketing.review.daniel.role',
    marketKey: 'marketing.review.daniel.market',
    initials: 'DO',
    rating: 5,
    quoteKey: 'marketing.review.daniel.quote',
  },
  {
    name: 'Camille Roux',
    roleKey: 'marketing.review.camille.role',
    marketKey: 'marketing.review.camille.market',
    initials: 'CR',
    rating: 4,
    quoteKey: 'marketing.review.camille.quote',
  },
  {
    name: 'Priya Nadar',
    roleKey: 'marketing.review.priya.role',
    marketKey: 'marketing.review.priya.market',
    initials: 'PN',
    rating: 5,
    quoteKey: 'marketing.review.priya.quote',
  },
];

export const MARKETING_REVIEW_STATS: readonly MarketingStat[] = [
  {value: '4.8 / 5', labelKey: 'marketing.reviewStat.averageRating'},
  {value: '94%', labelKey: 'marketing.reviewStat.wouldRecommend'},
  {value: '18 months', labelKey: 'marketing.reviewStat.medianTime'},
];

export const MARKETING_PLANS: readonly MarketingPlan[] = [
  {nameKey: 'marketing.plan.starter.name', monthlyPrice: 29, descriptionKey: 'marketing.plan.starter.description', actionKey: 'marketing.plan.starter.action'},
  {
    nameKey: 'marketing.plan.growth.name',
    monthlyPrice: 99,
    descriptionKey: 'marketing.plan.growth.description',
    actionKey: 'marketing.plan.growth.action',
    featured: true,
  },
  {nameKey: 'marketing.plan.pro.name', monthlyPrice: 299, descriptionKey: 'marketing.plan.pro.description', actionKey: 'marketing.plan.pro.action'},
];

export const MARKETING_CHANNELS: readonly MarketingChannel[] = [
  {
    icon: 'envelope',
    titleKey: 'marketing.channel.email.title',
    detailKey: 'marketing.channel.email.detail',
    value: 'hello@cvhome.co',
    href: 'mailto:hello@cvhome.co',
  },
  {
    icon: 'messageCircle',
    titleKey: 'marketing.channel.chat.title',
    detailKey: 'marketing.channel.chat.detail',
    value: 'Avg. 4 min',
  },
  {
    icon: 'phone',
    titleKey: 'marketing.channel.call.title',
    detailKey: 'marketing.channel.call.detail',
    value: '+31 20 241 8890',
    href: 'tel:+31202418890',
  },
];

export const CONTACT_TOPICS: readonly ContactTopic[] = [
  {id: 'migratingStores', labelKey: 'marketing.contactTopic.migratingStores'},
  {id: 'newMarketSetup', labelKey: 'marketing.contactTopic.newMarketSetup'},
  {id: 'customPlan', labelKey: 'marketing.contactTopic.customPlan'},
  {id: 'somethingElse', labelKey: 'marketing.contactTopic.somethingElse'},
];

/** The placeholder key the template shows in the message box for each topic. */
export const CONTACT_MESSAGE_HINT_KEYS: Readonly<Record<string, string>> = {
  migratingStores: 'marketing.contactHint.migratingStores',
  newMarketSetup: 'marketing.contactHint.newMarketSetup',
  customPlan: 'marketing.contactHint.customPlan',
  somethingElse: 'marketing.contactHint.somethingElse',
};
