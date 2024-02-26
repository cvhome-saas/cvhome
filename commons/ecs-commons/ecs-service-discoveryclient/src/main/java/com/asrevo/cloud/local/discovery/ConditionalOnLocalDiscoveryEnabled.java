package com.asrevo.cloud.local.discovery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

/**
 * @author ashraf
 * used to check if local discovery is enabled
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ConditionalOnProperty(value = "spring.cloud.local.discovery.enabled", matchIfMissing = true)
public @interface ConditionalOnLocalDiscoveryEnabled {
}
