/* (C)2013-2020 */
package com.asrevo.cvhome.s2s.config.gateway;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.*;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author Spencer Gibb
 */
public class FHostRoutePredicateFactory
        extends AbstractRoutePredicateFactory<FHostRoutePredicateFactory.Config> {

    public FHostRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("host");
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange exchange) {
                return Optional.ofNullable(exchange.getRequest().getHeaders().getHost())
                        .map(address -> config.host.contains(address.getHostName()))
                        .orElse(false);
            }

            @Override
            public Object getConfig() {
                return config;
            }

            @Override
            public String toString() {
                return String.format("Hosts: %s", config.getHost());
            }
        };
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Config {
        private Set<String> host;
    }
}
