package com.asrevo.cvhome.catalog.config;

import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientsConfig {
    @Bean
    public ExternalMerchantStoreService externalMerchantStoreService(
            RestClientBuilder restClientBuilder) {
        ExternalMerchantStoreService externalMerchantStoreService =
                restClientBuilder.buildClient("merchant", ExternalMerchantStoreService.class);
        return new CachedExternalMerchantStoreService(externalMerchantStoreService);
    }
}
