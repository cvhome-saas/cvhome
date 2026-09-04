package com.asrevo.cvhome.merchant.api;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.asrevo.cvhome.s2s.services.StoreOrgOwnerRetriever;

/**
 * Gives a pod service the store-to-organization lookup its authorization needs, by having the merchant client.
 *
 * <p>
 * Registered rather than declared per service because the alternative is worse: without it,
 * {@code StoreRoleAccessChecker} refuses every org admin, and a service that forgot to declare a bean would fail
 * in a way that reads as a permissions bug. With it, having the client is enough.
 * </p>
 */
@AutoConfiguration
@ConditionalOnBean(ExternalMerchantStoreService.class)
public class MerchantStoreOrgOwnerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(StoreOrgOwnerRetriever.class)
    StoreOrgOwnerRetriever merchantStoreOrgOwner(ExternalMerchantStoreService stores) {
        return new MerchantStoreOrgOwner(stores);
    }

}
