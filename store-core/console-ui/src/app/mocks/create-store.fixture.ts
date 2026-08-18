import type {ArtifactItem, CountryOption, NextStepLink} from '@models/create-store';

export const COUNTRIES: readonly CountryOption[] = [
  {id: 'DE', labelKey: 'createStore.country.de', currencyCode: 'EUR', currencyLabelKey: 'createStore.currency.eur'},
  {id: 'US', labelKey: 'createStore.country.us', currencyCode: 'USD', currencyLabelKey: 'createStore.currency.usd'},
  {id: 'AE', labelKey: 'createStore.country.ae', currencyCode: 'AED', currencyLabelKey: 'createStore.currency.aed'},
  {id: 'EG', labelKey: 'createStore.country.eg', currencyCode: 'EGP', currencyLabelKey: 'createStore.currency.egp'},
  {id: 'SG', labelKey: 'createStore.country.sg', currencyCode: 'SGD', currencyLabelKey: 'createStore.currency.sgd'},
];

export const DEFAULT_COUNTRY_ID = 'DE';

export const PROVISIONING_ARTIFACTS: readonly ArtifactItem[] = [
  {
    id: 'database',
    labelKey: 'createStore.artifact.database.label',
    detailKey: 'createStore.artifact.database.detail',
    icon: 'database',
  },
  {
    id: 'node',
    labelKey: 'createStore.artifact.node.label',
    detailKey: 'createStore.artifact.node.detail',
    icon: 'server',
  },
  {
    id: 'domain',
    labelKey: 'createStore.artifact.domain.label',
    detailKey: 'createStore.artifact.domain.detail',
    icon: 'globe',
  },
  {
    id: 'locale',
    labelKey: 'createStore.artifact.locale.label',
    detailKey: 'createStore.artifact.locale.detail',
    icon: 'percent',
  },
  {
    id: 'owner',
    labelKey: 'createStore.artifact.owner.label',
    detailKey: 'createStore.artifact.owner.detail',
    icon: 'user',
  },
];

export const NEXT_STEPS: readonly NextStepLink[] = [
  {id: 'logo', labelKey: 'createStore.nextStep.logo', icon: 'palette', route: '/store-management/branding'},
  {id: 'domain', labelKey: 'createStore.nextStep.domain', icon: 'globe', route: '/store-management/domain'},
  {id: 'payments', labelKey: 'createStore.nextStep.payments', icon: 'creditCard', route: '/store-management/payments'},
  {id: 'product', labelKey: 'createStore.nextStep.product', icon: 'box'},
];
