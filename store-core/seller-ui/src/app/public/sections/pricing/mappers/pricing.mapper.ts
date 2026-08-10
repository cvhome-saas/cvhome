import {PlanPriceView, PlanView} from 'seller-core/subscriptions';
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

/**
 * A catalog plan as the public pricing page renders it.
 *
 * The shape is unchanged from when this page read control-plane's org-level plan tables, so the template did not have
 * to move with the data source. What changed underneath is where plans come from: billing's public catalog, which is
 * the same list the seller console and Stripe are driven from, so a marketing page can no longer quietly disagree
 * with what a customer is actually charged.
 */
export function toPricing(plan: PlanView, price: PlanPriceView): Pricing {
  return {
    id: price.id,
    name: plan.displayName,
    cost: `${price.amount.minorUnits / 100}`,
    // Billing publishes one price per plan and interval; there is no "was" price to strike through. Kept equal so
    // the template's discount branch simply never fires rather than needing a second shape.
    previousCost: `${price.amount.minorUnits / 100}`,
    url: '',
    icon: `${PRICING_BASE_IMG_PATH}${plan.code.toLowerCase()}.png`,
    pricingFeatures: featuresOf(plan)
  };
}

/**
 * The entitlements a plan grants, as feature lines.
 *
 * A key a plan omits means unlimited, and a capability set to false is simply not a feature — so neither is listed.
 * Listing an omitted key would read as the opposite of what it means.
 */
function featuresOf(plan: PlanView): PricingFeature[] {
  return Object.entries(plan.entitlements)
    .filter(([, value]) => value.flagValue !== false)
    .map(([key]) => ({desc: key}));
}
