/*
package com.asrevo.cvhome.s2s.config.internal;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Slf4j
public class S2SResponseTimeLoggingFilter implements ExchangeFilterFunction {

    private static final Meter METER = GlobalOpenTelemetry.getMeter("s2s.client");
    private static final DoubleHistogram RESPONSE_TIME_HISTOGRAM = METER.histogramBuilder("s2s.client.request.duration")
            .setDescription("The duration of S2S client requests")
            .setUnit("ms")
            .build();

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        long startTime = System.currentTimeMillis();
        return next.exchange(request).doOnNext(clientResponse -> {
            long duration = System.currentTimeMillis() - startTime;
            log.info("WebClient request to {} completed in {} ms with status code: {}",
                    request.url(), duration, clientResponse.statusCode());
            RESPONSE_TIME_HISTOGRAM.record((double) duration, Attributes.of(
                    Attributes.key("client.type").string("WebClient"),
                    Attributes.key("http.url").string(request.url().toString()),
                    Attributes.key("http.status_code").longValue((long) clientResponse.statusCode().value())
            ));
        });
    }
}
*/
