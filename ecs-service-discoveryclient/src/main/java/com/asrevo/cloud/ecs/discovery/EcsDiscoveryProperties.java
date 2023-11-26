package com.asrevo.cloud.ecs.discovery;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(EcsDiscoveryProperties.PREFIX)
@Getter
@Setter
public class EcsDiscoveryProperties {
    public static final String PREFIX = "spring.cloud.ecs.discovery";
    private String namespace;
    private boolean enabled = true;
}
