package com.asrevo.cvhome.podregistry.api;

import java.util.List;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.commons.domain.Pod;

import reactor.core.publisher.Mono;

/**
 * The pod list, for the reactive gateway.
 *
 * <p>
 * Returns {@link Pod} rather than {@code PodView}: the gateway needs an id, a name and an endpoint to build a route
 * and nothing else, and {@code Pod} is already the type it deserializes. Sending it the registry's full view would
 * couple the edge of the platform to columns it has no use for — lifecycle, capacity, health.
 * </p>
 *
 * <p>
 * Reactor types are confined to this interface. Servlet callers get their own, so a {@code Mono} never appears on a
 * blocking caller's proxy.
 * </p>
 */
@HttpExchange("/api/v1/pod")
public interface ReactiveExternalPodService {

    @GetExchange("list")
    Mono<List<Pod>> listPods();

}
