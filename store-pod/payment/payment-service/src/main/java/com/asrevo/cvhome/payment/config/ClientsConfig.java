package com.asrevo.cvhome.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    private static final String MERCHANT_SERVICE_NAME = "merchant";
    private static final String CHECKOUT_SERVICE_NAME = "checkout";

    @Bean
    public ExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
        ExternalMerchantStoreService externalMerchantStoreService = restClientBuilder.buildClient(MERCHANT_SERVICE_NAME,
                ExternalMerchantStoreService.class, RemoteErrorCatalog.none());
        return new CachedExternalMerchantStoreService(externalMerchantStoreService);
    }

    @Bean
    public ExternalOrderService externalOrderService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(CHECKOUT_SERVICE_NAME,
                ExternalOrderService.class, RemoteErrorCatalog.none());
    }

}
