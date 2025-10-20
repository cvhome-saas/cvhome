package com.asrevo.cvhome.content.config;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class ApiUsageAop {
    @Autowired private Environment environment;

    @Around("@annotation(status)")
    public Object allow(ProceedingJoinPoint joinPoint, ConditionalOnApiStatus status)
            throws Throwable {
        String calling =
                joinPoint.getSignature().getDeclaringTypeName()
                        + "."
                        + joinPoint.getSignature().getName();
        boolean isDisabled =
                switch (status.usage()) {
                    case UNKNOWN -> isUnknownDisabled();
                    case USED -> isUsedDisabled();
                    case WILL_USED -> isWillUsedDisabled();
                    case NOT_USED -> isNotUsedDisabled();
                    case WILL_NOT_USED -> isWillNotUsedDisabled();
                    case null -> isUnknownDisabled();
                };

        if (isDisabled) {
            log.info("will disable api for {}", calling);
            throw new RuntimeException();
        }
        return joinPoint.proceed();
    }

    private boolean isWillUsedDisabled() {
        return environment.getProperty(
                "com.asrevo.endpoints.will-used.disabled", Boolean.class, false);
    }

    private boolean isUsedDisabled() {
        return environment.getProperty("com.asrevo.endpoints.used.disabled", Boolean.class, false);
    }

    private boolean isNotUsedDisabled() {
        return environment.getProperty(
                "com.asrevo.endpoints.not-used.disabled", Boolean.class, false);
    }

    private boolean isWillNotUsedDisabled() {
        return environment.getProperty(
                "com.asrevo.endpoints.will-not-used.disabled", Boolean.class, false);
    }

    private boolean isUnknownDisabled() {
        return environment.getProperty(
                "com.asrevo.endpoints.unknown.disabled", Boolean.class, false);
    }
}
