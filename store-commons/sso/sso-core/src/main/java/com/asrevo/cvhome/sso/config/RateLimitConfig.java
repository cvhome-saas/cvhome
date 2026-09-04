package com.asrevo.cvhome.sso.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.asrevo.cvhome.errors.web.ProblemDetailFactory;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.ratelimit.RateLimitFilter;
import com.asrevo.cvhome.sso.ratelimit.RateLimitProperties;

import tools.jackson.databind.ObjectMapper;

/** Registers the rate limiter ahead of the security filter chain. */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 100;

    @Bean
    FilterRegistrationBean<RateLimitFilter> rateLimitFilter(RateLimitProperties properties, ProblemDetailFactory problems,
                                                            ObjectMapper json, AuditService audit) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(
                new RateLimitFilter(properties, problems, json, audit));
        registration.setOrder(ORDER);
        registration.addUrlPatterns("/login", "/oauth2/token", "/api/v1/public/*");
        return registration;
    }

}
