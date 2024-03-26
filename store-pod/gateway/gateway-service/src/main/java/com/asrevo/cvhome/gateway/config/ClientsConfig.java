package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.gateway.service.DomainReferenceService;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientsConfig {
    @Bean
    public DomainReferenceService domainReferenceService(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("dcm", DomainReferenceService.class);
    }

}
