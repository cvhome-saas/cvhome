package com.asrevo.cvhome.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.billing.services.entitlement.ReactiveExternalEntitlementService;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.podregistry.api.ReactiveExternalPodService;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;

@Configuration
public class ClientsConfig {

    /**
     * The pod list now comes from pod-registry rather than tenancy.
     *
     * <p>
     * This is what the split was for. Tenancy is the busiest service in store-core and the one most often
     * redeployed, and the gateway's whole tenant route table used to hang off it; pod-registry holds one table that
     * changes when infrastructure changes, which is to say almost never.
     * </p>
     *
     * <p>
     * {@code RemoteErrorCatalog.none()} for the same reason as billing below: {@code PodClient} treats every failure
     * identically — keep the last known routes — so there is nothing for a catalog to distinguish.
     * </p>
     */
    @Bean
    public ReactiveExternalPodService reactivePodService(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("pod-registry", ReactiveExternalPodService.class,
                RemoteErrorCatalog.none());
    }

    /**
     * Built from the reactive half of billing's entitlement contract, because the gateway is reactive.
     *
     * <p>
     * {@code RemoteErrorCatalog.none()}: the only caller treats any failure the same way — keep the last known set and
     * carry on — so there is nothing for a catalog to tell apart. Mapping codes here would suggest the gateway acts
     * differently on them, and it must not.
     * </p>
     */
    @Bean
    public ReactiveExternalEntitlementService reactiveEntitlementService(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("billing", ReactiveExternalEntitlementService.class,
                RemoteErrorCatalog.none());
    }

}
