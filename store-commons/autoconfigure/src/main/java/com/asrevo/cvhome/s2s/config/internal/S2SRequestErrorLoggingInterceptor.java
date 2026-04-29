/*
package com.asrevo.cvhome.s2s.config.internal;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
public class S2SRequestErrorLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Meter METER = GlobalOpenTelemetry.getMeter("s2s.client");
    private static final LongCounter ERROR_COUNTER = METER.counterBuilder("s2s.client.request.errors")
            .setDescription("Counts the number of S2S client request errors")
            .setUnit("1")
            .build();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        if (response.getStatusCode().isError()) {
            log.error("RestClient request to {} failed with status code: {}", request.getURI(), response.getStatusCode());
            ERROR_COUNTER.add(1, Attributes.of(
                    Attributes.key("client.type").string("RestClient"),
                    Attributes.key("http.url").string(request.getURI().toString()),
                    Attributes.key("http.status_code").longValue((long) response.getStatusCode().value())
            ));
        }
        return response;
    }
}
*/
