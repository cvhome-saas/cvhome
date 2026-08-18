import type {BillingInterval, EntitlementKey, PlanPriceView, PlanView} from '@models/billing';
import type {PlanFeature, PricingPlan} from '@models/marketing';

/**
 * Ported and rebuilt from seller-ui/src/app/public/sections/pricing/mappers/pricing.mapper.ts.
 *
 * Turns billing's public catalog into the cards the pricing section draws. The catalog is the same list the console
 * and Stripe are driven from, so a marketing page can no longer quietly disagree with what a customer is charged.
 *
 * What changed from the seller-ui original: it listed entitlements by printing the raw enum name (`MAX_PRODUCTS`) and
 * dropped every ceiling's value on the floor. `EntitlementKey` is a fixed seven-key enum, so the console labels all of
 * them properly and shows the numbers — see `ENTITLEMENT_ORDER` below.
 */

/**
 * The order features are listed in, and the reason this is a list rather than a walk of the response: an omitted key
 * means *unlimited*, so the keys a plan does not mention are exactly the ones worth shouting about. Walking the
 * response would silently drop them, which is how the old page ended up showing PRO as the plan with the fewest
 * features.
 */
const ENTITLEMENT_ORDER: readonly EntitlementKey[] = [
  'MAX_PRODUCTS',
  'MAX_ORDERS_MONTH',
  'MAX_ACCOUNTS',
  'MAX_STORAGE_MB',
  'CUSTOM_DOMAIN',
  'ANALYTICS',
  'PRIORITY_SUPPORT',
];

/**
 * The plans purchasable at one billing interval, as cards.
 *
 * A plan with no price at the requested interval is not shown at all rather than shown as unavailable: the catalog's
 * FREE plan is monthly-only, and "Free — not available yearly" is a worse answer than leaving the yearly view to the
 * plans that can actually be bought yearly.
 */
interface PricedPlan {
  readonly plan: PlanView;
  readonly price: PlanPriceView;
}

export function toPricingPlans(plans: readonly PlanView[], interval: BillingInterval): PricingPlan[] {
  const priced = [...plans]
    .sort((a, b) => a.tier - b.tier)
    .flatMap((plan) => {
      const price = plan.prices.find((it) => it.interval === interval);
      return price ? [{plan, price} satisfies PricedPlan] : [];
    });

  const featuredCode = middleCode(priced);

  return priced.map(({plan, price}) => ({
    priceId: price.id.id,
    code: plan.code,
    name: plan.displayName,
    description: plan.description,
    amount: price.amount.minorUnits / 100,
    currency: price.amount.currency.code,
    interval: price.interval,
    trialDays: price.trialDays,
    features: featuresOf(plan),
    free: price.amount.minorUnits === 0,
    featured: plan.code === featuredCode,
  }));
}

/**
 * Which card the section highlights.
 *
 * Billing publishes no "recommended plan" flag, so this is a rule about the row and nothing more: the middle card,
 * where a pricing page conventionally puts its emphasis. With fewer than three cards nothing is highlighted, because
 * "the middle of two" is a choice the page has no basis to make — which is exactly the yearly view, where the
 * monthly-only free plan drops out.
 *
 * See lessons.md, "Marketing — no recommended-plan flag in the billing catalog".
 */
function middleCode(entries: readonly PricedPlan[]): string | null {
  return entries.length >= 3 ? entries[Math.floor((entries.length - 1) / 2)].plan.code : null;
}

/**
 * What the plan grants, as feature lines.
 *
 * Three cases, and they are genuinely different statements:
 * - the key is missing → the catalog means unlimited;
 * - `flagValue === false`, or a ceiling of 0 → the plan does not grant it, so it is not a feature and is not listed;
 * - otherwise → granted, with the ceiling if it has one.
 */
function featuresOf(plan: PlanView): PlanFeature[] {
  const features: PlanFeature[] = [];

  for (const key of ENTITLEMENT_ORDER) {
    const value = plan.entitlements[key];

    if (value === undefined) {
      features.push({key, limit: null, unlimited: true});
      continue;
    }
    if (value.flagValue === false || value.limitValue === 0) {
      continue;
    }
    features.push({key, limit: value.limitValue, unlimited: false});
  }

  return features;
}
