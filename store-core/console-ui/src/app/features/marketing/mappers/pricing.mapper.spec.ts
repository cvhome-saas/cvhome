import type {EntitlementValue, PlanView} from '@models/billing';
import {toPricingPlans} from './pricing.mapper';

function limit(key: EntitlementValue['key'], value: number): EntitlementValue {
  return {key, limitValue: value, flagValue: null};
}

function flag(key: EntitlementValue['key'], value: boolean): EntitlementValue {
  return {key, limitValue: null, flagValue: value};
}

/** The catalog as `billing-service/src/main/resources/plan-catalog.yml` seeds it. */
const CATALOG: PlanView[] = [
  {
    id: {id: 'plan-free'},
    code: 'FREE',
    displayName: 'Free',
    description: 'A store to try the platform with.',
    tier: 0,
    prices: [{id: {id: 'price-free-m'}, amount: {currency: {code: 'USD'}, minorUnits: 0}, interval: 'MONTH', trialDays: 0}],
    entitlements: {
      MAX_PRODUCTS: limit('MAX_PRODUCTS', 25),
      MAX_ORDERS_MONTH: limit('MAX_ORDERS_MONTH', 50),
      MAX_ACCOUNTS: limit('MAX_ACCOUNTS', 1),
      MAX_STORAGE_MB: limit('MAX_STORAGE_MB', 500),
      CUSTOM_DOMAIN: flag('CUSTOM_DOMAIN', false),
      ANALYTICS: flag('ANALYTICS', false),
      PRIORITY_SUPPORT: flag('PRIORITY_SUPPORT', false),
    },
  },
  {
    id: {id: 'plan-basic'},
    code: 'BASIC',
    displayName: 'Basic',
    description: 'For a store finding its feet.',
    tier: 10,
    prices: [
      {id: {id: 'price-basic-m'}, amount: {currency: {code: 'USD'}, minorUnits: 1000}, interval: 'MONTH', trialDays: 14},
      {id: {id: 'price-basic-y'}, amount: {currency: {code: 'USD'}, minorUnits: 10000}, interval: 'YEAR', trialDays: 14},
    ],
    entitlements: {
      MAX_PRODUCTS: limit('MAX_PRODUCTS', 500),
      MAX_ORDERS_MONTH: limit('MAX_ORDERS_MONTH', 1000),
      MAX_ACCOUNTS: limit('MAX_ACCOUNTS', 3),
      MAX_STORAGE_MB: limit('MAX_STORAGE_MB', 5000),
      CUSTOM_DOMAIN: flag('CUSTOM_DOMAIN', true),
      ANALYTICS: flag('ANALYTICS', false),
      PRIORITY_SUPPORT: flag('PRIORITY_SUPPORT', false),
    },
  },
  {
    id: {id: 'plan-pro'},
    code: 'PRO',
    displayName: 'Pro',
    description: 'For a store that is growing.',
    tier: 20,
    prices: [
      {id: {id: 'price-pro-m'}, amount: {currency: {code: 'USD'}, minorUnits: 3000}, interval: 'MONTH', trialDays: 0},
      {id: {id: 'price-pro-y'}, amount: {currency: {code: 'USD'}, minorUnits: 30000}, interval: 'YEAR', trialDays: 0},
    ],
    // MAX_PRODUCTS and MAX_ORDERS_MONTH are omitted, which the catalog defines as unlimited.
    entitlements: {
      MAX_ACCOUNTS: limit('MAX_ACCOUNTS', 10),
      MAX_STORAGE_MB: limit('MAX_STORAGE_MB', 50000),
      CUSTOM_DOMAIN: flag('CUSTOM_DOMAIN', true),
      ANALYTICS: flag('ANALYTICS', true),
      PRIORITY_SUPPORT: flag('PRIORITY_SUPPORT', true),
    },
  },
];

describe('toPricingPlans', () => {
  it('orders plans by tier and converts minor units to major', () => {
    const plans = toPricingPlans(CATALOG, 'MONTH');

    expect(plans.map((plan) => plan.code)).toEqual(['FREE', 'BASIC', 'PRO']);
    expect(plans.map((plan) => plan.amount)).toEqual([0, 10, 30]);
    expect(plans[1].currency).toBe('USD');
    expect(plans[1].priceId).toBe('price-basic-m');
  });

  it('drops plans that are not sold at the requested interval', () => {
    // FREE is monthly-only in the catalog, so the yearly view simply has no free plan.
    expect(toPricingPlans(CATALOG, 'YEAR').map((plan) => plan.code)).toEqual(['BASIC', 'PRO']);
    expect(toPricingPlans(CATALOG, 'YEAR').map((plan) => plan.amount)).toEqual([100, 300]);
  });

  it('marks the free plan by price, not by code', () => {
    const renamed = CATALOG.map((plan) => (plan.code === 'FREE' ? {...plan, code: 'STARTER'} : plan));
    const plans = toPricingPlans(renamed, 'MONTH');

    expect(plans.find((plan) => plan.free)?.code).toBe('STARTER');
    expect(plans.filter((plan) => plan.free).length).toBe(1);
  });

  it('lists an omitted ceiling as unlimited rather than dropping it', () => {
    const pro = toPricingPlans(CATALOG, 'MONTH').find((plan) => plan.code === 'PRO');

    expect(pro?.features.find((feature) => feature.key === 'MAX_PRODUCTS'))
      .toEqual({key: 'MAX_PRODUCTS', limit: null, unlimited: true});
    expect(pro?.features.find((feature) => feature.key === 'MAX_ORDERS_MONTH')?.unlimited).toBeTrue();
  });

  it('omits capabilities the plan does not grant', () => {
    const free = toPricingPlans(CATALOG, 'MONTH').find((plan) => plan.code === 'FREE');

    expect(free?.features.map((feature) => feature.key))
      .toEqual(['MAX_PRODUCTS', 'MAX_ORDERS_MONTH', 'MAX_ACCOUNTS', 'MAX_STORAGE_MB']);
  });

  it('omits a ceiling of zero, which means none allowed rather than unlimited', () => {
    const capped = CATALOG.map((plan) =>
      plan.code === 'FREE' ? {...plan, entitlements: {...plan.entitlements, MAX_PRODUCTS: limit('MAX_PRODUCTS', 0)}} : plan,
    );
    const free = toPricingPlans(capped, 'MONTH').find((plan) => plan.code === 'FREE');

    expect(free?.features.some((feature) => feature.key === 'MAX_PRODUCTS')).toBeFalse();
  });

  it('highlights the middle card, and nothing when fewer than three are shown', () => {
    // Monthly shows all three, so the middle one is emphasised.
    expect(toPricingPlans(CATALOG, 'MONTH').find((plan) => plan.featured)?.code).toBe('BASIC');
    // Yearly drops the monthly-only free plan, leaving two cards and no defensible middle.
    expect(toPricingPlans(CATALOG, 'YEAR').find((plan) => plan.featured)).toBeUndefined();
  });

  it('carries the trial length through so the call to action can name it', () => {
    const plans = toPricingPlans(CATALOG, 'MONTH');

    expect(plans.find((plan) => plan.code === 'BASIC')?.trialDays).toBe(14);
    expect(plans.find((plan) => plan.code === 'PRO')?.trialDays).toBe(0);
  });
});
