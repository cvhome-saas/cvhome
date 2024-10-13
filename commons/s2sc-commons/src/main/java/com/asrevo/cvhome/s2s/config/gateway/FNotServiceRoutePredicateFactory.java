/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                String[] parts =
                        Stream.of(splits).filter(it -> !it.isEmpty()).toArray(String[]::new);
                if (parts.length > 0) {
                    return !config.services.contains(parts[0]);
                } else {
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
    public record Config(Set<String> services) {}
}
