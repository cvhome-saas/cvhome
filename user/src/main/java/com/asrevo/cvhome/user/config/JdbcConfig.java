package com.asrevo.cvhome.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

@Configuration
@EnableR2dbcAuditing
public class JdbcConfig {
    @Bean
    public ReactiveAuditorAware<String> auditorAware() {
        return () -> {
            return ReactiveSecurityContextHolder.getContext().map(it -> it.getAuthentication().getName());
        };
    }
}
