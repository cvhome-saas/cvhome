import type {
  ArtifactItem,
  CountryOption,
  HostingRegionOption,
  NextStepLink,
  ProvisioningTask,
  StorePlanOption,
} from '@models/create-store';

export const COUNTRIES: readonly CountryOption[] = [
  {id: 'DE', labelKey: 'createStore.country.de', currencyCode: 'EUR', currencyLabelKey: 'createStore.currency.eur'},
  {id: 'US', labelKey: 'createStore.country.us', currencyCode: 'USD', currencyLabelKey: 'createStore.currency.usd'},
  {id: 'AE', labelKey: 'createStore.country.ae', currencyCode: 'AED', currencyLabelKey: 'createStore.currency.aed'},
  {id: 'EG', labelKey: 'createStore.country.eg', currencyCode: 'EGP', currencyLabelKey: 'createStore.currency.egp'},
  {id: 'SG', labelKey: 'createStore.country.sg', currencyCode: 'SGD', currencyLabelKey: 'createStore.currency.sgd'},
];

export const DEFAULT_COUNTRY_ID = 'DE';

export const HOSTING_REGIONS: readonly HostingRegionOption[] = [
  {
    id: 'eu',
    labelKey: 'createStore.region.eu.label',
    code: 'eu-central-1',
    metaKey: 'createStore.region.meta.gdpr',
    metaParams: {nodes: 6},
    latencyMs: 18,
    recommended: true,
    locked: false,
    residencyKey: 'createStore.region.eu.residency',
  },
  {
    id: 'us',
    labelKey: 'createStore.region.us.label',
    code: 'us-west-2',
    metaKey: 'createStore.region.meta.shared',
    metaParams: {store: 'Acme Supply Co.', nodes: 11},
    latencyMs: 142,
    recommended: false,
    locked: false,
    residencyKey: 'createStore.region.us.residency',
  },
  {
    id: 'me',
    labelKey: 'createStore.region.me.label',
    code: 'me-south-1',
    metaKey: 'createStore.region.meta.arabicPack',
    metaParams: {nodes: 3},
    latencyMs: 96,
    recommended: false,
    locked: false,
    residencyKey: 'createStore.region.me.residency',
  },
  {
    id: 'ap',
    labelKey: 'createStore.region.ap.label',
    code: 'ap-southeast-1',
    metaKey: 'createStore.region.meta.proOnly',
    latencyMs: 210,
    recommended: false,
    locked: true,
    residencyKey: 'createStore.region.ap.residency',
  },
];

export const DEFAULT_REGION_ID = 'eu';

export const STORE_PLANS: readonly StorePlanOption[] = [
  {
    id: 'starter',
    labelKey: 'createStore.plan.starter',
    monthlyPrice: 29,
    specs: [
      {key: 'createStore.plan.spec.cpuRam', params: {cpu: 1, ram: 2}},
      {key: 'createStore.plan.spec.mediaStorage', params: {gb: 5}},
      {key: 'createStore.plan.spec.ordersPerMonth', params: {count: 10000}},
      {key: 'createStore.plan.spec.sharedNode'},
    ],
  },
  {
    id: 'growth',
    labelKey: 'createStore.plan.growth',
    monthlyPrice: 99,
    specs: [
      {key: 'createStore.plan.spec.cpuRam', params: {cpu: 2, ram: 8}},
      {key: 'createStore.plan.spec.mediaStorage', params: {gb: 50}},
      {key: 'createStore.plan.spec.ordersPerMonth', params: {count: 100000}},
      {key: 'createStore.plan.spec.dedicatedNode'},
    ],
  },
  {
    id: 'pro',
    labelKey: 'createStore.plan.pro',
    monthlyPrice: 299,
    specs: [
      {key: 'createStore.plan.spec.cpuRam', params: {cpu: 8, ram: 32}},
      {key: 'createStore.plan.spec.mediaStorage', params: {gb: 500}},
      {key: 'createStore.plan.spec.ordersUnlimited'},
      {key: 'createStore.plan.spec.dedicatedHotStandby'},
    ],
  },
];

export const DEFAULT_PLAN_ID = 'growth';

/** Runs in this order; the running phase marks them done one at a time. */
export const PROVISIONING_TASKS: readonly ProvisioningTask[] = [
  {
    id: 'validate',
    labelKey: 'createStore.task.validate.label',
    detailKey: 'createStore.task.validate.detail',
  },
  {
    id: 'allocate',
    labelKey: 'createStore.task.allocate.label',
    detailKey: 'createStore.task.allocate.detail',
  },
  {
    id: 'database',
    labelKey: 'createStore.task.database.label',
    detailKey: 'createStore.task.database.detail',
  },
  {
    id: 'seed',
    labelKey: 'createStore.task.seed.label',
    detailKey: 'createStore.task.seed.detail',
  },
  {
    id: 'tls',
    labelKey: 'createStore.task.tls.label',
    detailKey: 'createStore.task.tls.detail',
  },
  {
    id: 'cdn',
    labelKey: 'createStore.task.cdn.label',
    detailKey: 'createStore.task.cdn.detail',
    detailParams: {edges: 18, gb: 50},
  },
  {
    id: 'live',
    labelKey: 'createStore.task.live.label',
    detailKey: 'createStore.task.live.detail',
  },
];

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
