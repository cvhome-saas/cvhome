package com.asrevo.cvhome.billing.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.EntitlementKey;

/**
 * The declared plan catalog, read from {@code plan-catalog.yml}.
 *
 * <p>
 * This is the source the seeder reconciles the database against, not the runtime source of truth — once seeded, the
 * tables are what everything reads, because they also carry the Stripe ids that only Stripe can mint.
 * </p>
 *
 * @param seedEnabled        whether to reconcile the database against this file on start
 * @param stripeSyncEnabled  whether to also create the matching Stripe products and prices
 * @param plans              the declared plans
 */
@ConfigurationProperties("com.asrevo.cvhome.billing.catalog")
public record PlanCatalogProperties(boolean seedEnabled, boolean stripeSyncEnabled, List<Plan> plans) {

    public PlanCatalogProperties {
        plans = plans == null ? List.of() : plans;
    }

    /**
     * @param code         the stable handle; renaming it mints a different plan rather than editing this one
     * @param displayName  what a pricing page shows
     * @param description  the marketing line
     * @param tier         ordering — higher is an upgrade. Keep distinct, or "is this an upgrade" has no answer.
     * @param prices       what it can be bought at
     * @param entitlements what it grants; an omitted key means unlimited, a key set to 0 means none
     */
    public record Plan(String code, String displayName, String description, Integer tier, List<Price> prices,
                       Map<EntitlementKey, String> entitlements) {

        public Plan {
            prices = prices == null ? List.of() : prices;
            entitlements = entitlements == null ? Map.of() : entitlements;
        }

    }

    /**
     * @param currency  ISO-4217 code
     * @param amount    the recurring charge in minor units
     * @param interval  how often it recurs
     * @param trialDays free days this price grants on its own, on top of the org-level trial
     */
    public record Price(String currency, Long amount, BillingInterval interval, Integer trialDays) {

        public Price {
            trialDays = trialDays == null ? 0 : trialDays;
        }

    }

}
