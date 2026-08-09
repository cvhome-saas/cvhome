import {FreeOption, Option} from 'seller-core';
import {PRICING_BASE_IMG_PATH} from '../constants/pricing.constants';

export interface PricingFeature {
  desc: string;
}

export interface PriceId {
  id: string;
}

export interface Pricing {
  id?: PriceId;
  name: string;
  icon: string;
  cost: string;
  previousCost?: string;
  pricingFeatures: PricingFeature[];
  url: string;
}

export function toPricing(option: Option): Pricing {
  return {
    id: option.id,
    name: option.subscriptionPlan,
    cost: `${option.cost.price / 100}`,
    previousCost: `${option.previousCost.price / 100}`,
    url: '',
    icon: `${PRICING_BASE_IMG_PATH}${option.subscriptionPlan.toLowerCase()}.png`,
    pricingFeatures: option.feature.features.map((it) => ({desc: it.code}))
  };
}

export function toFreePricing(freeOption: FreeOption): Pricing {
  return {
    name: freeOption.subscriptionPlan,
    cost: `${freeOption.cost.price / 100}`,
    url: '',
    icon: `${PRICING_BASE_IMG_PATH}${freeOption.subscriptionPlan.toLowerCase()}.png`,
    pricingFeatures: freeOption.feature.features.map((it) => ({desc: it.code}))
  };
}
