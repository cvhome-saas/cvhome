package com.asrevo.cvhome.controlplane.config;

import java.net.http.HttpClient;

import com.asrevo.cvhome.commons.http.HttpClientCustomizer;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.instrumentation.javahttpclient.JavaHttpClientTelemetry;
import io.opentelemetry.api.OpenTelemetry;

@Configuration
public class HttpClientTelemetryConfig {

    @Bean
    @SneakyThrows
    @ConditionalOnClass({OpenTelemetry.class, JavaHttpClientTelemetry.class})
    @ConditionalOnMissingBean(HttpClientCustomizer.class)
    public HttpClientCustomizer uaaHttpClientCustomizer(org.springframework.beans.factory.ObjectProvider<OpenTelemetry> openTelemetryProvider) {
        OpenTelemetry openTelemetry = openTelemetryProvider.getIfAvailable();
        if (openTelemetry != null) {
            return builder -> JavaHttpClientTelemetry.builder(openTelemetry).build().wrap(builder.build());
        }
        return HttpClient.Builder::build;
    }

    @Bean
    @ConditionalOnMissingBean(HttpClientCustomizer.class)
    public HttpClientCustomizer defaultHttpClientCustomizer() {
        return HttpClient.Builder::build;
    }
}
