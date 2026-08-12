package com.asrevo.cvhome.billing.services.entitlement;

import java.util.List;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import reactor.core.publisher.Mono;

/**
 * The gateway's half of the entitlement contract.
 *
 * <p>
 * Separate from {@link ExternalEntitlementService} rather than sharing one interface with reactive return types,
 * because the gateway is the only reactive caller. Putting {@code Mono} on the shared contract would force reactor
 * onto every servlet caller's proxy, and putting caller-side types on the server interface is the same mistake in
 * the other direction.
 * </p>
 *
 * <p>
 * Only the blocked-store list is exposed here: it is all the gateway needs, and it is small enough to hold in memory
 * and refresh on a timer.
 * </p>
 */
@HttpExchange("/api/v1/entitlement/private")
public interface ReactiveExternalEntitlementService {

    @GetExchange("/blocked-stores")
    Mono<List<StoreMerchantId>> blockedStores();

}
