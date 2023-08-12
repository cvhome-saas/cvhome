package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.gateway.service.AcmService;
import com.asrevo.cvhome.gateway.service.DomainReferenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebConfig {
    @Bean
    DomainReferenceService domainReferenceService(WebClient.Builder builder) {
        WebClient client = builder
                .baseUrl("http://localhost:8082")
                .build();
        HttpServiceProxyFactory proxy =
                HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client))
                        .build();
        return proxy.createClient(DomainReferenceService.class);
    }

    @Bean
    AcmService acmService(WebClient.Builder builder) {
        WebClient client = builder
                .baseUrl("http://localhost:8082")
                .build();
        HttpServiceProxyFactory proxy =
                HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client))
                        .build();
        return proxy.createClient(AcmService.class);
    }
}
