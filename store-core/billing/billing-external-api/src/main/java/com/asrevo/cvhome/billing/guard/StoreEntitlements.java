package com.asrevo.cvhome.billing.guard;

import java.time.Duration;
import java.util.function.Supplier;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.errors.EntitlementExceededException;
import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

/**
 * The pod-side guard: may this store be worked in, and is it within its plan's ceilings?
 *
 * <p>
 * Lives in the client SDK rather than in each pod because every pod needs the same three behaviours, and getting any
 * of them slightly different per service is how enforcement becomes inconsistent: a short cache so a write path does
 * not call billing every time, a hard rule about what happens when billing is unreachable, and one exception type
 * for "you are at your limit".
 * </p>
 *
 * <p>
 * <b>It degrades open.</b> When billing cannot be reached, the last known snapshot is used; when there is none, the
 * store is treated as operable with no ceilings. A merchant who is paying must not stop trading because a service
 * they never heard of is down, and the money already collected is a stronger signal than our ability to check it.
 * The opposite choice belongs at store creation, where nothing has been paid for yet.
 * </p>
 *
 * <p>
 * Not a Spring bean by itself: each pod builds one in its own {@code ClientsConfig}, so a service that does not
 * enforce anything does not silently acquire a dependency on billing.
 * </p>
 */
@Slf4j
public class StoreEntitlements {

    private final ExternalEntitlementService entitlementService;

    private final Cache<StoreMerchantId, EntitlementSnapshot> cache;

    /**
     * Last known answers, kept beyond the live cache so an outage has something to fall back to rather than nothing.
     */
    private final Cache<StoreMerchantId, EntitlementSnapshot> lastKnown;

    public StoreEntitlements(ExternalEntitlementService entitlementService, Duration ttl) {
        this.entitlementService = entitlementService;
        this.cache = Caffeine.newBuilder().expireAfterWrite(ttl).maximumSize(10_000L).build();
        this.lastKnown = Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(6L)).maximumSize(10_000L).build();
    }

    /**
     * What the store is entitled to, as far as we can tell.
     *
     * <p>
     * Never throws. A caller on a write path needs an answer, and "billing is unavailable" is not an answer it can
     * act on — so the fallback is baked in here rather than left to each call site to remember.
     * </p>
     */
    public EntitlementSnapshot snapshot(StoreMerchantId store) {
        StoreMerchantId key = new StoreMerchantId(store.storeMerchantId());
        EntitlementSnapshot cached = cache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }
        return fetch(key);
    }

    /**
     * Whether the store may be written to at all.
     */
    public boolean operable(StoreMerchantId store) {
        return snapshot(store).operable();
    }

    /**
     * Refuses an action that would take the store past a ceiling its plan grants.
     *
     * <p>
     * The current count is supplied by the caller rather than read here, because only the owning service can count
     * its own rows — and counting them is often the expensive part, so it is deferred behind a {@link Supplier} and
     * never evaluated for a plan with no ceiling on that key.
     * </p>
     *
     * @param current how many the store already has, evaluated only if a ceiling applies
     * @throws EntitlementExceededException the store is at its limit
     */
    public void require(StoreMerchantId store, EntitlementKey key, Supplier<Integer> current)
            throws EntitlementExceededException {
        EntitlementValue value = snapshot(store).entitlement(key);
        if (value.unlimited() || !key.numeric()) {
            return;
        }
        int held = current.get();
        if (value.exceeded(held)) {
            throw EntitlementExceededException.of(store, key, value.limitValue(), held);
        }
    }

    /**
     * Refuses an action a plan does not include at all — a capability rather than a ceiling.
     *
     * @throws EntitlementExceededException the plan does not grant it
     */
    public void requireGranted(StoreMerchantId store, EntitlementKey key) throws EntitlementExceededException {
        if (!snapshot(store).entitlement(key).granted()) {
            throw EntitlementExceededException.of(store, key, 0, 0);
        }
    }

    private EntitlementSnapshot fetch(StoreMerchantId key) {
        try {
            EntitlementSnapshot fresh = entitlementService.snapshot(key);
            cache.put(key, fresh);
            lastKnown.put(key, fresh);
            return fresh;
        } catch (Exception e) {
            EntitlementSnapshot stale = lastKnown.getIfPresent(key);
            if (stale != null) {
                // The cause is logged, not just the symptom: this line is the only evidence that enforcement has
                // silently stopped, and "could not reach billing" without a reason sends whoever reads it hunting
                // for a network problem that may well be a 403.
                log.warn("Could not reach billing for store {}; using the last known entitlements", key, e);
                return stale;
            }
            log.warn("Could not reach billing for store {} and have never seen it; allowing the action", key, e);
            return EntitlementSnapshot.degradedOpen(key);
        }
    }

}
