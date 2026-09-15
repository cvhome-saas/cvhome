package com.asrevo.cvhome.merchant.api;

import java.time.Duration;

import com.asrevo.cvhome.cache.CacheRegion;

/**
 * The region of the merchant client's cached read, declared once here and brought to every service that has the
 * client ({@link MerchantStoreOrgOwnerAutoConfiguration}).
 */
public enum MerchantClientRegions implements CacheRegion {

    /** A store's record as merchant answers it: currency, units, languages, the login rule. */
    STORE_CLIENT(Names.STORE_CLIENT, Duration.ofMinutes(5), 10_000);

    private final String name;

    private final Duration ttl;

    private final long maxSize;

    MerchantClientRegions(String name, Duration ttl, long maxSize) {
        this.name = name;
        this.ttl = ttl;
        this.maxSize = maxSize;
    }

    @Override
    public String regionName() {
        return name;
    }

    @Override
    public Class<?> valueType() {
        return com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore.class;
    }

    @Override
    public Duration ttl() {
        return ttl;
    }

    @Override
    public long maxSize() {
        return maxSize;
    }

    /** The names, as {@code @Cacheable} needs a constant. */
    public static final class Names {

        public static final String STORE_CLIENT = "merchant.store-client";

        private Names() {
        }
    }
}
