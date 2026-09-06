package com.asrevo.cvhome.checkout.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Adds the shopper resolver beside the platform's store/language/pageable resolvers from {@code ServletWebConfig}.
 */
@Configuration
public class CheckoutWebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ShopperArgumentResolver());
    }
}
