package com.asrevo.cvhome.gateway;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}

record DomainReference(@Id String domain, String reference) {
}

interface DomainReferenceRepository extends ReactiveCrudRepository<DomainReference, String> {

}

@Component
@Slf4j
class MapHostAsRequestParamGatewayFilterFactory extends AbstractGatewayFilterFactory<MapHostAsRequestParamGatewayFilterFactory.Config> {
    public static final String TEMPLATE_KEY = "template";
    private final DomainReferenceRepository repository;
    private final String MapHostAsRequestParamHeader = "Map-Host-As-Request-Param";

    public MapHostAsRequestParamGatewayFilterFactory(DomainReferenceRepository repository) {
        super(MapHostAsRequestParamGatewayFilterFactory.Config.class);
        this.repository = repository;
    }

    public List<String> shortcutFieldOrder() {
        return List.of(TEMPLATE_KEY);
    }


    @Override
    public GatewayFilter apply(MapHostAsRequestParamGatewayFilterFactory.Config config) {
        return (exchange, chain) -> {
            URI uri = exchange.getRequest().getURI();
            String hostName = exchange.getRequest().getHeaders().getHost().getHostName();
            if (checkingIfHostMappingNeeded(exchange, config)) {
                Mono<String> domainReference = this.repository.findById(hostName).map(DomainReference::domain);
                try {
                    return domainReference.flatMap(itx -> {
                        if (itx != null && !itx.isEmpty()) {
                            ServerHttpRequest request = createHttpRequestWithNewParams(config, exchange, uri, itx);
                            return chain.filter(exchange.mutate().request(request).build());
                        } else {
                            log.warn("will reject " + uri + " with headers " + exchange.getRequest().getHeaders());
                            return response(exchange, HttpStatus.BAD_REQUEST, "invalid host header " + hostName);
                        }
                    });
                } catch (RuntimeException ex) {
                    throw new IllegalStateException("Invalid URI query: \"" + uri.getRawQuery() + "\"");
                }
            } else {
                return chain.filter(exchange);
            }
        };
    }

    private static Mono<Void> response(ServerWebExchange exchange, HttpStatus httpStatus, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(message.getBytes())));
    }

    private static ServerHttpRequest createHttpRequestWithNewParams(Config config, ServerWebExchange exchange, URI uri, String itx) {
        StringBuilder query = new StringBuilder();
        String originalQuery = uri.getRawQuery();
        if (StringUtils.hasText(originalQuery)) {
            query.append(originalQuery);
            if (originalQuery.charAt(originalQuery.length() - 1) != '&') {
                query.append('&');
            }
        }
        String value = ServerWebExchangeUtils.expand(exchange, itx);
        query.append(config.getTemplate());
        query.append('=');
        query.append(value);
        URI newUri = fromUri(uri).replaceQuery(query.toString()).build(true).toUri();
        return exchange.getRequest().mutate().uri(newUri).build();
    }

    public boolean checkingIfHostMappingNeeded(ServerWebExchange exchange, MapHostAsRequestParamGatewayFilterFactory.Config config) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        return headers.containsKey(MapHostAsRequestParamHeader);
    }

    @Getter
    @Setter
    public static class Config {
        private String template;
    }
}
