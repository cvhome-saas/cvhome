package com.asrevo.cvhome.catalog.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.billing.api.errors.BillingApiErrors;
import com.asrevo.cvhome.billing.guard.StoreEntitlements;
import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

/**
 * Wires this pod to billing's entitlement API.
 *
 * <p>
 * Declared here rather than in {@code store-commons:autoconfigure} on purpose: a service that enforces nothing
 * should not acquire a dependency on billing merely by being on the classpath. Enforcement is opted into, per pod,
 * and this file is the opt-in.
 * </p>
 */
@Configuration
public class BillingGuardConfig {

    private static final String BILLING_SERVICE_NAME = "billing";

    @Bean
    public ExternalEntitlementService externalEntitlementService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(BILLING_SERVICE_NAME, ExternalEntitlementService.class,
                BillingApiErrors.CATALOG);
    }

    /**
     * The guard every write path consults.
     *
     * <p>
     * The TTL is how long a store that has just lapsed keeps working here, and how long one that has just paid stays
     * locked out. A minute is short enough that neither is noticed and long enough that a busy catalog does not call
     * billing on every save.
     * </p>
     */
    @Bean
    public StoreEntitlements storeEntitlements(ExternalEntitlementService entitlementService,
                                               @Value("${com.asrevo.cvhome.billing.guard.ttl:PT60S}") Duration ttl) {
        return new StoreEntitlements(entitlementService, ttl);
    }

}
