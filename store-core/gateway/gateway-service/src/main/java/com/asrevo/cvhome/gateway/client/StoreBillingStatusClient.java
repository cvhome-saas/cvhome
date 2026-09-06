package com.asrevo.cvhome.gateway.client;

import java.util.HashSet;
import java.util.Set;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.services.entitlement.ReactiveExternalEntitlementService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;

/**
 * Keeps the set of stores that may not be worked in, refreshed on a timer.
 *
 * <p>
 * Held in memory and polled rather than asked per request, for the same reason {@link PodClient} polls its routes:
 * this is consulted on every seller request, and a call to billing in that path would make billing's latency the
 * gateway's latency and billing's downtime the gateway's downtime.
 * </p>
 *
 * <p>
 * The cost of polling is staleness — a store suspended a moment ago keeps working until the next refresh. That is
 * accepted deliberately: the alternative charges every request for a rare event, and a minute of grace on a
 * subscription that has already lapsed harms nobody.
 * </p>
 *
 * <p>
 * The sharpest consequence, verified rather than assumed: a gateway that <em>starts</em> while billing is down holds
 * an empty set and therefore blocks nothing until billing answers. Every lapsed store keeps working for that window.
 * That follows from failing open and is the intended trade — an outage in billing must not take the seller console
 * offline for everyone — but it does mean this gate is a convenience for the common case, not a guarantee. Anything
 * that must hold under a billing outage belongs in the pods, where the data being protected is.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class StoreBillingStatusClient {

    private final ReactiveExternalEntitlementService entitlementService;

    /**
     * Replaced wholesale on each refresh rather than mutated, so a request reading it never sees a half-built set.
     */
    private volatile Set<String> blockedStores = Set.of();

    /**
     * Reactive for the same reason as {@link PodClient#refreshRoutes()}: Spring subscribes outside the scheduled-task
     * observation scope, which is what stops the per-refresh scope-leak warning.
     */
    @Scheduled(fixedRateString = "${cvhome.gateway.billing-refresh-rate:PT1M}", initialDelay = 5000L)
    public Mono<Void> refresh() {
        // Deferred for the same reason as PodClient.refreshRoutes(): the publisher is obtained once at startup.
        return Mono.defer(entitlementService::blockedStores)
                .doOnNext(this::replace)
                .onErrorResume(e -> {
                    // Fails open, and this is the deliberate opposite of store creation, which fails closed. An
                    // outage in billing must not take every working storefront's console offline; the worst case
                    // here is that a lapsed store keeps working a while longer, which is recoverable. Keeping the
                    // last known set rather than clearing it also means a blip does not unblock everyone.
                    log.error("Could not refresh blocked stores from billing; keeping the last known set", e);
                    return Mono.empty();
                })
                .then();
    }

    /**
     * Whether this store is currently barred.
     */
    public boolean blocked(String store) {
        return store != null && blockedStores.contains(store);
    }

    private void replace(java.util.List<StoreMerchantId> stores) {
        Set<String> refreshed = new HashSet<>(stores.size());
        stores.forEach(it -> refreshed.add(it.storeMerchantId()));
        if (refreshed.size() != blockedStores.size()) {
            log.info("Blocked store set changed: {} -> {}", blockedStores.size(), refreshed.size());
        }
        this.blockedStores = Set.copyOf(refreshed);
    }

}
