package com.asrevo.cvhome.payment.reads;

import java.time.Duration;

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

/** What payment caches: the payment types a store accepts, read by every checkout page. */
public enum PaymentRegions implements CacheRegion {

    TYPES(Names.TYPES);

    private final String name;

    PaymentRegions(String name) {
        this.name = name;
    }

    @Override
    public String regionName() {
        return name;
    }

    @Override
    public Class<?> valueType() {
        return PaymentType[].class;
    }

    @Override
    public Duration ttl() {
        return Duration.ofSeconds(60);
    }

    @Override
    public long maxSize() {
        return 10_000;
    }

    /** The names, as {@code @Cacheable} needs a constant. */
    public static final class Names {

        public static final String TYPES = "payment.types";

        private Names() {
        }
    }
}
