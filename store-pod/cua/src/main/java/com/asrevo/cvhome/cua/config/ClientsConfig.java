package com.asrevo.cvhome.cua.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.content.api.ExternalBrandingService;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    @Bean
    public ExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
        ExternalMerchantStoreService externalMerchantStoreService = restClientBuilder.buildClient("merchant",
                ExternalMerchantStoreService.class, RemoteErrorCatalog.none());
        return new CachedExternalMerchantStoreService(externalMerchantStoreService);
    }

    /**
     * The store's brand imagery, for the logo on the login and register pages. It used to be a field on the
     * merchant record these pages already read; appearance moved to the content service and it went with it.
     */
    @Bean
    public ExternalBrandingService externalBrandingService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient("content", ExternalBrandingService.class, RemoteErrorCatalog.none());
    }

}
