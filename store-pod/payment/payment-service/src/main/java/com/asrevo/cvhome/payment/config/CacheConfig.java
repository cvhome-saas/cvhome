package com.asrevo.cvhome.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.eviction.EvictionRules;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.reads.PaymentRegions;

/** What payment caches ({@link PaymentRegions}) and what drops it: a store's payment configuration. */
@Configuration
public class CacheConfig {

    @Bean
    CacheRegions paymentRegions() {
        return CacheRegions.of(PaymentRegions.values());
    }

    @Bean
    EvictionRules paymentEvictionRules() {
        return EvictionRules.in("com.asrevo.cvhome.payment.entity")
                .on(PaymentConfiguration.class).evict(PaymentRegions.TYPES)
                .build();
    }
}
