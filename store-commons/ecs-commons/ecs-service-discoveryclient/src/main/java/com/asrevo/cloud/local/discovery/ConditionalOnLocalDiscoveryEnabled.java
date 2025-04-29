package com.asrevo.cloud.local.discovery;

import java.lang.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * @author ashraf
 * used to check if local discovery is enabled
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ConditionalOnProperty(value = "spring.cloud.local.discovery.enabled")
public @interface ConditionalOnLocalDiscoveryEnabled {}
