package com.asrevo.cvhome.store.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;


@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Autowired
    private MerchantStoreArgumentResolver merchantStoreArgumentResolver;

    @Autowired
    private LanguageArgumentResolver languageArgumentResolver;


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(merchantStoreArgumentResolver);
        argumentResolvers.add(languageArgumentResolver);
    }
}
