package com.asrevo.cvhome.cache.event;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.eviction.EvictionRules;

/**
 * What a received cache event does to this task's regions: drops the store's entries of every region the service's
 * {@link EvictionRules} map the event to, the same eviction a commit of the entity would have done here.
 *
 * <p>
 * One class for both directions. The service that raised the event applies it from its outbox handler (a second
 * time over its own commit eviction, which is free); a service that receives a foreign event through a transport
 * applies it the same way, with the rules it declared for that event. An event no rule names is logged once at
 * DEBUG and drops nothing: a service is never made to clear every store by an event it did not opt into.
 * </p>
 */
public final class CacheEventApplier {

    private static final Logger LOG = LoggerFactory.getLogger(CacheEventApplier.class);

    private final EvictionRules rules;

    private final CacheRegistry registry;

    public CacheEventApplier(EvictionRules rules, CacheRegistry registry) {
        this.rules = rules;
        this.registry = registry;
    }

    /** Applies {@code event} and returns the regions it dropped, empty when no rule names its type. */
    public Set<CacheRegion> apply(CacheEvent event) {
        Set<CacheRegion> regions = rules.regionsForEvent(event.getClass());
        if (regions.isEmpty()) {
            LOG.debug("cache event {} of store {} maps to no region here", event.eventType(), event.store().getId());
            return regions;
        }
        registry.evictStore(event.store(), regions);
        return regions;
    }
}
