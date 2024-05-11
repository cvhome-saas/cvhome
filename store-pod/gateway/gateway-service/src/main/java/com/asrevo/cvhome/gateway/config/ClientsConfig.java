package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.s2s.clients.RouterAllocationService;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientsConfig {
    @Bean
    public RouterAllocationService routerAllocationService(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("manager", RouterAllocationService.class);
    }
}
