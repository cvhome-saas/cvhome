package com.asrevo.cvhome.controlplane.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.billing.api.errors.BillingApiErrors;
import com.asrevo.cvhome.billing.services.quota.ExternalStoreQuotaService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    private static final String BILLING_SERVICE_NAME = "billing";

    /**
     * Built from {@code ExternalStoreQuotaService}, the caller-side half of billing's contract — never from
     * {@code IStoreQuotaService}, whose {@code throws} clauses are billing's own vocabulary.
     *
     * <p>
     * {@code BillingApiErrors.CATALOG} is what makes a refusal arrive as a different type from a billing service that
     * could not be answered by. Store creation acts on that distinction: a refusal is shown to the org, an
     * unreachable billing service fails the request so it can be retried rather than quietly creating a store nobody
     * is billed for.
     * </p>
     */
    @Bean
    public ExternalStoreQuotaService externalStoreQuotaService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(BILLING_SERVICE_NAME, ExternalStoreQuotaService.class,
                BillingApiErrors.CATALOG);
    }

}
