package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.s2s.config.gateway.FHostRoutePredicateFactory;
import com.asrevo.cvhome.s2s.config.gateway.FNotServiceRoutePredicateFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(AbstractRoutePredicateFactory.class)
public class ReactiveGatewayConfig {

	@ConditionalOnClass(RoutePredicateHandlerMapping.class)
	@Bean
	public FHostRoutePredicateFactory fHostRoutePredicateFactory() {
		return new FHostRoutePredicateFactory();
	}

	@ConditionalOnClass(RoutePredicateHandlerMapping.class)
	@Bean
	public FNotServiceRoutePredicateFactory fPathNotApiRoutePredicateFactory() {
		return new FNotServiceRoutePredicateFactory();
	}

}
