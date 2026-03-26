package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class GatewayRouteLocatorImpl implements RouteLocator {

	private final RouteLocatorBuilder routeLocatorBuilder;

	private final ServiceDomainProperties serviceDomainProperties;

	private final Environment environment;

	@Override
	public Flux<Route> getRoutes() {
		String gatewayService = environment.getProperty("spring.application.name");
		String storeCoreGatewayDomain = serviceDomainProperties.getService(gatewayService).domain();
		String[] backendServices = { "/control-plane/**", "/uaa/**", "/store-pod-gateway/**" };

		return routeLocatorBuilder.routes()
			.route(r -> r.path("/control-plane/**")
				.filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
				.uri("lb://control-plane"))
			.route(r -> r.path(backendServices)
				.negate()
				.and()
				.host(storeCoreGatewayDomain, "www." + storeCoreGatewayDomain, "seller-ui." + storeCoreGatewayDomain)
				.uri("lb://seller-ui"))
			.build()
			.getRoutes();
	}

}
