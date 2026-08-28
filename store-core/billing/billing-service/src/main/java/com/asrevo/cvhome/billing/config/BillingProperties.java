package com.asrevo.cvhome.billing.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tunables of the billing rules themselves.
 *
 * @param trialPeriod the org's one trial, granted to the first store it creates
 * @param pastDueGrace how long a failed renewal keeps working before the store is suspended
 * @param quota abuse guards on store creation
 */
@ConfigurationProperties("com.asrevo.cvhome.billing")
public record BillingProperties(Duration trialPeriod, Duration pastDueGrace, Quota quota) {

    public BillingProperties {
        trialPeriod = trialPeriod == null ? Duration.ofDays(14L) : trialPeriod;
        pastDueGrace = pastDueGrace == null ? Duration.ofDays(7L) : pastDueGrace;
        quota = quota == null ? new Quota(null) : quota;
    }

    /**
     * @param maxPendingStores how many never-paid-for stores an org may hold at once. Not a cap on stores it may own:
     *                         each store carries its own subscription and pays for itself.
     */
    public record Quota(Integer maxPendingStores) {

        public Quota {
            maxPendingStores = maxPendingStores == null ? 3 : maxPendingStores;
        }

    }

}
