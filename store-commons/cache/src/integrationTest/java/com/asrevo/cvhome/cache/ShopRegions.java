package com.asrevo.cvhome.cache;

import java.time.Duration;

/** The one region of the integration test. */
public enum ShopRegions implements CacheRegion {

    NAME(Names.NAME),
    SEARCH(Names.SEARCH);

    private final String name;

    ShopRegions(String name) {
        this.name = name;
    }

    @Override
    public String regionName() {
        return name;
    }

    @Override
    public Class<?> valueType() {
        return String.class;
    }

    @Override
    public Duration ttl() {
        return Duration.ofMinutes(1);
    }

    @Override
    public long maxSize() {
        return 100;
    }

    /** The names, as {@code @Cacheable} needs a constant. */
    public static final class Names {

        public static final String NAME = "shop.name";

        public static final String SEARCH = "shop.search";

        private Names() {
        }
    }
}
