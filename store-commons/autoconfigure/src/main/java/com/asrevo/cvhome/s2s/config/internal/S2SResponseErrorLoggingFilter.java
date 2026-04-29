/*
package com.asrevo.cvhome.s2s.config.internal;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Slf4j
public class S2SResponseErrorLoggingFilter implements ExchangeFilterFunction {

    private static final Meter METER = GlobalOpenTelemetry.getMeter("s2s.client");
    private static final LongCounter ERROR_COUNTER = METER.counterBuilder("s2s.client.request.errors")
            .setDescription("Counts the number of S2S client request errors")
            .setUnit("1")
            .build();

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request).map(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                log.error("WebClient request to {} failed with status code: {}", request.url(), clientResponse.statusCode());
                ERROR_COUNTER.add(1, Attributes.of(
                        Attributes.key("client.type").string("WebClient"),
                        Attributes.key("http.url").string(request.url().toString()),
                        Attributes.key("http.status_code").longValue((long) clientResponse.statusCode().value())
                ));
            }
            return clientResponse;
        });
    }
}
*/
