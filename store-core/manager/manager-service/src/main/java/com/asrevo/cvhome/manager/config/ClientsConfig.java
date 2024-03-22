package com.asrevo.cvhome.manager.config;

import com.asrevo.cvhome.manager.service.RouterService;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ClientsConfig {
    @Bean
    public RouterService routerService(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("router", RouterService.class);
    }
}
