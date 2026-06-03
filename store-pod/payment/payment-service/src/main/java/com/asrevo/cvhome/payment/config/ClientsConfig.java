package com.asrevo.cvhome.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    private static final String MERCHANT_SERVICE_NAME = "merchant";

    @Bean
    public ExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
        ExternalMerchantStoreService externalMerchantStoreService = restClientBuilder.buildClient(MERCHANT_SERVICE_NAME,
                ExternalMerchantStoreService.class);
        return new CachedExternalMerchantStoreService(externalMerchantStoreService);
    }

}
