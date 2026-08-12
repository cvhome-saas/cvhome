package com.asrevo.cvhome.tenancy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.billing.api.errors.BillingApiErrors;
import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.billing.services.quota.ExternalStoreQuotaService;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryApiErrors;
import com.asrevo.cvhome.podregistry.services.placement.ExternalPodPlacementService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    private static final String BILLING_SERVICE_NAME = "billing";

    private static final String POD_REGISTRY_SERVICE_NAME = "pod-registry";

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

    /**
     * Reads store billing standing for the seller console.
     *
     * <p>
     * Unlike the quota client, every caller of this one degrades rather than fails: a store list that cannot reach
     * billing shows the standing as unknown instead of erroring.
     * </p>
     */
    @Bean
    public ExternalEntitlementService externalEntitlementService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(BILLING_SERVICE_NAME, ExternalEntitlementService.class,
                BillingApiErrors.CATALOG);
    }

    /**
     * Asks the pod registry where a new store should go.
     *
     * <p>
     * Blocking, from {@code ExternalPodPlacementService} — the registry's reactive pod-list contract belongs to the
     * gateway and must not appear on a servlet caller's proxy.
     * </p>
     *
     * <p>
     * Same shape as the billing quota client above, and for the same reason: the catalog is what makes "the registry
     * has nowhere to put this" arrive as a different type from "the registry did not answer". Store creation acts on
     * that distinction — the first is shown to the operator, the second fails the request so it can be retried.
     * </p>
     */
    @Bean
    public ExternalPodPlacementService externalPodPlacementService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(POD_REGISTRY_SERVICE_NAME, ExternalPodPlacementService.class,
                PodRegistryApiErrors.CATALOG);
    }

}
