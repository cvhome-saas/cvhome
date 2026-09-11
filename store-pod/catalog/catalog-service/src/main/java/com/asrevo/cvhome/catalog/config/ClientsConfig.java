package com.asrevo.cvhome.catalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.content.api.ExternalMediaService;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    /**
     * The store record, for its units of measure. Cached: it changes rarely and every product read needs it.
     */
    @Bean
    // Declared as the caching class, not its interface: Spring builds the @Cacheable proxy ahead of time from the
    // declared type, and natively a bean declared as the interface was never proxied (the load test saw 0 STORE
    // lookups and every product mapping calling merchant).
    public CachedExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
        return new CachedExternalMerchantStoreService(restClientBuilder.buildClient("merchant",
                ExternalMerchantStoreService.class, RemoteErrorCatalog.none()));
    }

    /**
     * The media library. Not cached: an attach has to see the asset as it is right now, and the usage write is
     * the whole point of the call.
     */
    @Bean
    public ExternalMediaService externalMediaService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient("content", ExternalMediaService.class, RemoteErrorCatalog.none());
    }
}
