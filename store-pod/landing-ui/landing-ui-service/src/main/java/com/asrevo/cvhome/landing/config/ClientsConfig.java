package com.asrevo.cvhome.landing.config;

import com.asrevo.cvhome.landing.service.CategoryService;
import com.asrevo.cvhome.landing.service.ProductService;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ClientsConfig {
    @Bean
    public ProductService productService(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("store", ProductService.class);
    }

    @Bean
    public CategoryService categoryService(WebClientBuilder webClientBuilder) {
        return webClientBuilder.buildClient("store", CategoryService.class);
    }
}
