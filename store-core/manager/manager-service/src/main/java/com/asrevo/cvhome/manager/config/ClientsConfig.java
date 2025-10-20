package com.asrevo.cvhome.manager.config;

import com.asrevo.cvhome.merchant.api.ExternalReactiveMerchantStoreService;
import com.asrevo.cvhome.merchant.api.StorePodClient;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;
import com.asrevo.cvhome.subscription.api.SubscriptionHttpEventsService;
import com.asrevo.cvhome.subscription.api.SubscriptionPlanDetailsService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientsConfig {
    @Bean
    public StorePodClient storePodClient(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("merchant", StorePodClient.class);
    }

    @Bean
    public SubscriptionPlanDetailsService subscriptionPlanDetailsService(
            WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("subscription", SubscriptionPlanDetailsService.class);
    }

    @Bean
    public ExternalReactiveMerchantStoreService externalMerchantStoreService(
            WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("merchant", ExternalReactiveMerchantStoreService.class);
    }

    @Bean
    public SubscriptionHttpEventsService subscriptionHttpEventsService(
            WebClientBuilder webClientBuilder) {
        JsonMapper mapper =
                JsonMapper.builder()
                        .addModules(new JavaTimeModule(), new Jdk8Module())
                        .setDefaultTyping(
                                new StdTypeResolverBuilder()
                                        .init(JsonTypeInfo.Id.CLASS, null)
                                        .inclusion(JsonTypeInfo.As.WRAPPER_OBJECT))
                        .build();
        return webClientBuilder.buildClient(
                "subscription", SubscriptionHttpEventsService.class, mapper);
    }
}
