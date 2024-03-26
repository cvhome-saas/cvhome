package com.asrevo.cloud.ecs.discovery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ConditionalOnProperty(value = "spring.cloud.ecs.discovery.enabled")
public @interface ConditionalOnEcsDiscoveryEnabled {
}
