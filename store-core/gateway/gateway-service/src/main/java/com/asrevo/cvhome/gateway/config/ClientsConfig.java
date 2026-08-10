package com.asrevo.cvhome.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.billing.services.entitlement.ReactiveExternalEntitlementService;
import com.asrevo.cvhome.controlplane.pod.api.ExternalPodClient;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;

@Configuration
public class ClientsConfig {

    @Bean
    public ExternalPodClient externalPodClient(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("control-plane", ExternalPodClient.class, RemoteErrorCatalog.none());
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
