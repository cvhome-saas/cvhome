package com.asrevo.cvhome.checkout.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Only the merchant store read is cached ({@link CachedExternalMerchantStoreService}); prices and stock are never.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
