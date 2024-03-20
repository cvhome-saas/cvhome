package com.asrevo.cvhome.landing.config;

import com.asrevo.cvhome.landing.service.CategoryService;
import com.asrevo.cvhome.landing.service.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import static com.asrevo.cvhome.s2s.utils.WebClientsUtils.build;


@Configuration
public class ClientsConfig {


    @Bean
    public ProductService productService(@Qualifier("defaultBalancedBuilder") WebClient.Builder builder) {
        return build(builder, "lb://store", ProductService.class);
    }

    @Bean
    public CategoryService categoryService(@Qualifier("defaultBalancedBuilder") WebClient.Builder builder) {
        return build(builder, "lb://store", CategoryService.class);
    }
}
