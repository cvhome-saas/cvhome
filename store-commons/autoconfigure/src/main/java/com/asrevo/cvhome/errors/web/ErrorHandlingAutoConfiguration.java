package com.asrevo.cvhome.errors.web;

import jakarta.validation.ConstraintViolationException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Registers the shared error handling in every servlet-based cvhome service without any per-service wiring, which is
 * what lets eight services share one implementation instead of the four duplicated advices this replaces.
 *
 * <p>
 * Every bean is {@link ConditionalOnMissingBean}, so a service that genuinely needs different behaviour can define its
 * own and win.
 * </p>
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(ResponseEntityExceptionHandler.class)
@EnableConfigurationProperties(ErrorHandlingProperties.class)
public class ErrorHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProblemDetailFactory problemDetailFactory(ErrorHandlingProperties properties) {
        return new ProblemDetailFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalErrorHandler globalErrorHandler(ProblemDetailFactory factory) {
        return new GlobalErrorHandler(factory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(ConstraintViolationException.class)
    public ConstraintViolationErrorHandler constraintViolationErrorHandler(ProblemDetailFactory factory) {
        return new ConstraintViolationErrorHandler(factory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(AccessDeniedException.class)
    public SecurityErrorHandler securityErrorHandler(ProblemDetailFactory factory) {
        return new SecurityErrorHandler(factory);
    }

}
