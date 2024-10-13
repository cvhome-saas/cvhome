package com.asrevo.cvhome.manager.config;

import com.asrevo.cvhome.manager.service.StorePodClient;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientsConfig {
    @Bean
    public StorePodClient storePodClient(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("store", StorePodClient.class);
    }
}
