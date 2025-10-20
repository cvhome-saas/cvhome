/* (C)2013-2020 */
package com.asrevo.cvhome.s2s.config.gateway;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.http.server.RequestPath;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author Spencer Gibb
 */
public class FNotServiceRoutePredicateFactory
		extends AbstractRoutePredicateFactory<FNotServiceRoutePredicateFactory.Config> {

	public FNotServiceRoutePredicateFactory() {
		super(Config.class);
	}

	@Override
	public ShortcutType shortcutType() {
		return ShortcutType.GATHER_LIST;
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Collections.singletonList("services");
	}

	@Override
	public Predicate<ServerWebExchange> apply(Config config) {
		return new GatewayPredicate() {
			@Override
			public boolean test(ServerWebExchange exchange) {
				RequestPath path = exchange.getRequest().getPath();
				String[] splits = path.toString().split("/");
				String[] parts = Stream.of(splits).filter(it -> !it.isEmpty()).toArray(String[]::new);
				if (parts.length > 0) {
					return !config.services.contains(parts[0]);
				}
				else {
					return true;
				}
			}

			@Override
			public Object getConfig() {
				return config;
			}
		};
	}

	@Validated
	public record Config(Set<String> services) {
	}

}
