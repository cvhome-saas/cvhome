package com.asrevo.cvhome.content.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.billing.api.errors.BillingApiErrors;
import com.asrevo.cvhome.billing.guard.StoreEntitlements;
import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    @Bean
    public ExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient("merchant", ExternalMerchantStoreService.class,
                RemoteErrorCatalog.none());
    }

    @Bean
    public ExternalEntitlementService externalEntitlementService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient("billing", ExternalEntitlementService.class,
                BillingApiErrors.CATALOG);
    }

    @Bean
    public StoreEntitlements storeEntitlements(ExternalEntitlementService entitlementService,
                                               @Value("${com.asrevo.cvhome.billing.guard.ttl:PT60S}") Duration ttl) {
        return new StoreEntitlements(entitlementService, ttl);
    }

}
