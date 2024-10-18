package com.asrevo.cloud.ecs.discovery;

import java.lang.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ConditionalOnProperty(value = "spring.cloud.ecs.discovery.enabled")
public @interface ConditionalOnEcsDiscoveryEnabled {}
