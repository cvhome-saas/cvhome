package com.asrevo.cvhome.sso.realm;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Which realms this deployment serves, and how a new one comes into being.
 *
 * <p>
 * Cached, because it is asked on the way in to every request and the answer changes about as often as a store is
 * created. Only positive answers are cached: a realm that does not exist yet may exist a moment later, and caching
 * "no" would keep a newly created store broken for the length of the cache.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RealmRegistry {

    private static final Duration TTL = Duration.ofMinutes(10);

    private final RealmRepository realms;

    private final Cache<String, Boolean> known = Caffeine.newBuilder().expireAfterWrite(TTL).build();

    public boolean exists(RealmId realm) {
        if (Boolean.TRUE.equals(known.getIfPresent(realm.getId()))) {
            return true;
        }
        boolean found = realms.existsByIdAndEnabledTrue(realm.getId());
        if (found) {
            known.put(realm.getId(), true);
        }
        return found;
    }

    /**
     * Creates the realm if this deployment does not have it yet.
     *
     * <p>
     * <strong>Only ever call this for a realm the edge vouched for.</strong> The pod edge resolves the storefront
     * host to a store before the request arrives, so a {@code Store-Id} it set is a store that exists; a store id
     * out of a query parameter or a login form is whatever the caller typed, and creating a realm from one would
     * let anybody mint tenants.
     * </p>
     *
     * <p>
     * In its own transaction so that a realm created on the way in survives whatever the request goes on to do,
     * and so a request that fails does not roll the store back out of existence.
     * </p>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensure(RealmId realm) {
        if (exists(realm)) {
            return;
        }
        realms.save(new Realm(realm.getId(), null));
        known.put(realm.getId(), true);
        log.info("Realm {} registered on first request", realm.getId());
    }

}
