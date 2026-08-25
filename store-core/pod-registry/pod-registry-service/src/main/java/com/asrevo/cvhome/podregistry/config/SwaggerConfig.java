package com.asrevo.cvhome.podregistry.config;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;

/**
 * Renders the identifier value objects as plain strings in the OpenAPI document, which is what they are on the wire.
 *
 * <p>
 * Deliberately does <em>not</em> declare an {@code OpenAPI} bean: {@code store-commons:autoconfigure} already
 * supplies one, and a second makes springdoc's {@code openAPIBuilder} ambiguous, which fails the context at
 * start-up rather than at first request.
 * </p>
 */
@Configuration
@SuppressWarnings("java:S1118")
public class SwaggerConfig {

    static {
        SpringDocUtils.getConfig().replaceWithClass(PodId.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(ManagerOrgId.class, String.class);
    }

}
