import type {ArtifactItem, NextStepLink} from '@models/create-store';

/*
 * The five-country, five-currency list that used to live here is gone. It was a fixture standing in
 * for reference data the platform does not serve, and the create form now reads
 * `ReferenceDataService` — every ISO country and currency, named by `Intl` in the reader's language —
 * exactly as store management's details section does. A merchant trading from anywhere else could
 * not create a store at all while this list was the whole set.
 */

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
