package com.asrevo.cvhome.cache;

import java.time.Duration;

/**
 * One named cache a service reads through: what it holds, how long, how much, and whether its keys carry a store.
 *
 * <p>
 * A service declares its regions in one enum implementing this, so what it caches can be read in one place; the
 * name is the {@code cache} label on every meter and the key of a configuration override
 * ({@code com.asrevo.cvhome.cache.regions.<name>}), which is why it is dotted lowercase: {@code catalog.product}.
 * The time-to-live and size here are the code's defaults; configuration wins.
 * </p>
 */
public interface CacheRegion {

    /** {@code <service>.<read>}, lowercase, a hyphen inside a read: {@code catalog.cart-line}. Not {@code name()}: a region is an enum. */
    String regionName();

    /** What the region holds; the Spring bridge checks a cached value against it. */
    Class<?> valueType();

    /** How long an entry is trusted; the staleness another task may show after a write. */
    Duration ttl();

    /** The most entries the region keeps. */
    long maxSize();

    /** Whether every key carries a store (the rule) or none does (a reference list shared by every tenant). */
    default Scope scope() {
        return Scope.STORE;
    }

    /** What a region's keys are scoped by. */
    enum Scope {
        STORE, GLOBAL
    }
}
