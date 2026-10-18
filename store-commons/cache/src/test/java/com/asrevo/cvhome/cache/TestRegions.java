package com.asrevo.cvhome.cache;

import java.time.Duration;

/** Two store-scoped regions and one global, for the tests of this module. */
public enum TestRegions implements CacheRegion {

    PRODUCT("test.product", String.class, Duration.ofSeconds(60), 100, Scope.STORE),
    LISTING("test.listing", String.class, Duration.ofSeconds(10), 50, Scope.STORE),
    COUNTRY("test.country", String.class, Duration.ofHours(1), 10, Scope.GLOBAL);

    private final String name;

    private final Class<?> valueType;

    private final Duration ttl;

    private final long maxSize;

    private final Scope scope;

    TestRegions(String name, Class<?> valueType, Duration ttl, long maxSize, Scope scope) {
        this.name = name;
        this.valueType = valueType;
        this.ttl = ttl;
        this.maxSize = maxSize;
        this.scope = scope;
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
        return ttl;
    }

    @Override
    public long maxSize() {
        return maxSize;
    }

    @Override
    public Scope scope() {
        return scope;
    }
}
