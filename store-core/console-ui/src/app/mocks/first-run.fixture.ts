import type {
  FeatureVideo,
  GuideVideo,
  NextUpCard,
  PlanLimit,
  SetupStep,
} from '@models/first-run';

/** The onboarding path, in the order the console enforces it. */
export const FIRST_RUN_STEPS: readonly SetupStep[] = [
  {id: 'create-store', labelKey: 'firstRun.step.createStore.label', metaKey: 'firstRun.step.createStore.meta'},
  {id: 'add-products', labelKey: 'firstRun.step.addProducts.label', metaKey: 'firstRun.step.addProducts.meta'},
  {id: 'design-home', labelKey: 'firstRun.step.designHome.label', metaKey: 'firstRun.step.designHome.meta'},
  {id: 'connect-payments', labelKey: 'firstRun.step.connectPayments.label', metaKey: 'firstRun.step.connectPayments.meta'},
];

export const FIRST_RUN_FEATURE: FeatureVideo = {
  titleKey: 'firstRun.feature.title',
  copyKey: 'firstRun.feature.copy',
  durationKey: 'firstRun.feature.duration',
};

export const FIRST_RUN_GUIDES: readonly GuideVideo[] = [
  {
    id: 'csv-import',
    titleKey: 'firstRun.guide.csvImport.title',
    durationKey: 'firstRun.guide.csvImport.duration',
    sectionKey: 'firstRun.guide.section.products',
  },
  {
    id: 'home-page',
    titleKey: 'firstRun.guide.homePage.title',
    durationKey: 'firstRun.guide.homePage.duration',
    sectionKey: 'firstRun.guide.section.storefront',
  },
  {
    id: 'go-live',
    titleKey: 'firstRun.guide.goLive.title',
    durationKey: 'firstRun.guide.goLive.duration',
    sectionKey: 'firstRun.guide.section.payments',
  },
  {
    id: 'second-language',
    titleKey: 'firstRun.guide.secondLanguage.title',
    durationKey: 'firstRun.guide.secondLanguage.duration',
    sectionKey: 'firstRun.guide.section.content',
  },
];

export const FIRST_RUN_NEXT_UP: readonly NextUpCard[] = [
  {id: 'catalogue', titleKey: 'firstRun.nextUp.catalogue.title', copyKey: 'firstRun.nextUp.catalogue.copy', icon: 'box'},
  {id: 'storefront', titleKey: 'firstRun.nextUp.storefront.title', copyKey: 'firstRun.nextUp.storefront.copy', icon: 'layoutGrid'},
  {id: 'payments', titleKey: 'firstRun.nextUp.payments.title', copyKey: 'firstRun.nextUp.payments.copy', icon: 'creditCard'},
];

/** Free-plan allowances. Nothing is consumed yet beyond the operator's own seat. */
export const FIRST_RUN_LIMITS: readonly PlanLimit[] = [
  {
    id: 'stores',
    labelKey: 'firstRun.limit.stores',
    used: '0',
    cap: '1',
    pct: 0,
    noteKey: 'firstRun.limit.growthAllows',
    noteParams: {amount: '5'},
  },
  {
    id: 'products',
    labelKey: 'firstRun.limit.products',
    used: '0',
    cap: '100',
    pct: 0,
    noteKey: 'firstRun.limit.growthAllows',
    noteParams: {amount: '10,000'},
  },
  {
    id: 'seats',
    labelKey: 'firstRun.limit.seats',
    used: '1',
    cap: '2',
    pct: 50,
    noteKey: 'firstRun.limit.growthAllows',
    noteParams: {amount: '15'},
  },
  {
    id: 'storage',
    labelKey: 'firstRun.limit.storage',
    used: '0 GB',
    cap: '1 GB',
    pct: 0,
    noteKey: 'firstRun.limit.growthAllows',
    noteParams: {amount: '25 GB'},
  },
];

export const FIRST_RUN_TRIAL_DAYS = 14;
