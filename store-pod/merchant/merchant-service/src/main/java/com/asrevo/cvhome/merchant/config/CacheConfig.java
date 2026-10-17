package com.asrevo.cvhome.merchant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.event.StoreChanged;
import com.asrevo.cvhome.cache.eviction.EvictionRules;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.reads.MerchantRegions;

/** What merchant caches ({@link MerchantRegions}) and what drops it: the store row, and its {@link StoreChanged}. */
@Configuration
public class CacheConfig {

    @Bean
    CacheRegions merchantRegions() {
        return CacheRegions.of(MerchantRegions.values());
    }

    @Bean
    EvictionRules merchantEvictionRules() {
        return EvictionRules.in("com.asrevo.cvhome.merchant.entity")
                .on(MerchantStore.class).evict(MerchantRegions.values())
                .onEvent(StoreChanged.class).evict(MerchantRegions.values())
                .build();
    }
}
