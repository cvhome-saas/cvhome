import {ENTITLEMENT_ORDER, type BillingInterval, type PlanPriceView, type PlanView} from '@models/billing';
import type {PlanFeature, PricingPlan} from '@models/marketing';

/**
 * Ported and rebuilt from seller-ui/src/app/public/sections/pricing/mappers/pricing.mapper.ts.
 *
 * Turns billing's public catalog into the cards a pricing surface draws.
 *
 * Shared rather than owned by the marketing page, because it is no longer the only reader: the console's
 * plan dialog compares the same catalog for a signed-in operator. Two copies of "how the catalog becomes
 * cards" would be two places for the unlimited rule and the saving calculation to drift. The catalog is the same list the console
 * and Stripe are driven from, so a marketing page can no longer quietly disagree with what a customer is charged.
 *
 * What changed from the seller-ui original: it listed entitlements by printing the raw enum name (`MAX_PRODUCTS`) and
 * dropped every ceiling's value on the floor. `EntitlementKey` is a fixed seven-key enum, so the console labels all of
 * them properly and shows the numbers — see `ENTITLEMENT_ORDER` below.
 */

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
    savingPercent: savingAgainstMonthly(plan, price),
  }));
}

/**
 * What a yearly price saves against twelve monthly ones.
 *
 * Computed per plan rather than stated once on the billing toggle. The toggle used to read "Yearly −20%",
 * which was true of the old page because it multiplied an authored monthly price by 0.8. Against the real
 * catalog it is false: BASIC is $100 a year against $10 a month, which is 17%, not 20%. A discount is a claim
 * about money and has to come from the prices actually charged.
 */
function savingAgainstMonthly(plan: PlanView, price: PlanPriceView): number | null {
  if (price.interval !== 'YEAR') {
    return null;
  }
  const monthly = plan.prices.find((it) => it.interval === 'MONTH');
  if (!monthly || monthly.amount.minorUnits === 0) {
    return null;
  }
  const fullYear = monthly.amount.minorUnits * 12;
  const saved = Math.round(((fullYear - price.amount.minorUnits) / fullYear) * 100);
  return saved > 0 ? saved : null;
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
