package com.asrevo.cvhome.manager.config;

import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;
import com.asrevo.cvhome.merchant.api.StorePodClient;
import com.asrevo.cvhome.merchant.api.ExternalReactiveMerchantStoreService;
import com.asrevo.cvhome.subscription.api.SubscriptionPlanDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ClientsConfig {
    @Bean
    public StorePodClient storePodClient(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("merchant", StorePodClient.class);
    }

    @Bean
    public SubscriptionPlanDetailsService subscriptionPlanDetailsService(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("subscription", SubscriptionPlanDetailsService.class);
    }

    @Bean
    public ExternalReactiveMerchantStoreService externalMerchantStoreService(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("merchant", ExternalReactiveMerchantStoreService.class);
    }

}
