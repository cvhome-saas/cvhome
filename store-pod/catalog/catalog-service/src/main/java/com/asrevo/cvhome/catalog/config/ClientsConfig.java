package com.asrevo.cvhome.catalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    /**
     * The store record, for its units of measure. Cached: it changes rarely and every product read needs it.
     */
    @Bean
    public ExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
        return new CachedExternalMerchantStoreService(restClientBuilder.buildClient("merchant",
                ExternalMerchantStoreService.class, RemoteErrorCatalog.none()));
    }
}
