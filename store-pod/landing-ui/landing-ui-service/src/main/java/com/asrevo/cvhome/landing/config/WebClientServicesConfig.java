package com.asrevo.cvhome.landing.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientServicesConfig {

    @Bean("defaultBuilder")
    public WebClient.Builder defaultBuilder() {
        return WebClient.builder();
    }

    @Bean("defaultBalancedBuilder")
    @LoadBalanced
    public WebClient.Builder defaultBalancedBuilder() {
        return WebClient.builder();
    }


}
