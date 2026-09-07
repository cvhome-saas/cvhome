package com.asrevo.cvhome.gateway.errors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.errors.web.ErrorHandlingProperties;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

/**
 * The problem-detail factory, which the shared autoconfiguration registers for servlet services only. Same
 * properties, same type URIs, so a gateway error reads like every other service's.
 */
@Configuration
@EnableConfigurationProperties(ErrorHandlingProperties.class)
public class GatewayErrorConfig {

    @Bean
    ProblemDetailFactory problemDetailFactory(ErrorHandlingProperties properties) {
        return new ProblemDetailFactory(properties);
    }

}
