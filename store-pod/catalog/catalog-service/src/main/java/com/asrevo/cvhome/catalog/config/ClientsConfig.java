package com.asrevo.cvhome.catalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    @Bean
    public ExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
        ExternalMerchantStoreService externalMerchantStoreService = restClientBuilder.buildClient("merchant",
                ExternalMerchantStoreService.class);
        return new CachedExternalMerchantStoreService(externalMerchantStoreService);
    }

    @Bean
    public ExternalOrderService externalOrderService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient("checkout",
                ExternalOrderService.class);
    }

}
