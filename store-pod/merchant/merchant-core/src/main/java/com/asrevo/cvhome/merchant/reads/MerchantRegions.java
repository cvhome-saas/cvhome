package com.asrevo.cvhome.merchant.reads;

import java.time.Duration;
import java.util.List;

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

/**
 * What merchant caches: the store record its peers read on every call (five minutes, evicted the moment the store
 * is saved here), the storefront's shape of it per language, and the store's languages.
 */
public enum MerchantRegions implements CacheRegion {

    STORE(Names.STORE, ReadableMerchantStore.class, 10_000),
    STORE_BY_LANGUAGE(Names.STORE_BY_LANGUAGE, ReadableMerchantStore.class, 20_000),
    LANGUAGES(Names.LANGUAGES, List.class, 10_000);

    private final String name;

    private final Class<?> valueType;

    private final long maxSize;

    MerchantRegions(String name, Class<?> valueType, long maxSize) {
        this.name = name;
        this.valueType = valueType;
        this.maxSize = maxSize;
    }

    @Override
    public String regionName() {
        return name;
    }

    @Override
    public Class<?> valueType() {
        return valueType;
    }

    @Override
    public Duration ttl() {
        return Duration.ofMinutes(5);
    }

    @Override
    public long maxSize() {
        return maxSize;
    }

    /** The names, as {@code @Cacheable} needs a constant. */
    public static final class Names {

        public static final String STORE = "merchant.store";

        public static final String STORE_BY_LANGUAGE = "merchant.store-by-language";

        public static final String LANGUAGES = "merchant.languages";

        private Names() {
        }
    }
}
