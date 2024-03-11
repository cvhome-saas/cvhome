package com.asrevo.cloud.ecs.discovery;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(EcsDiscoveryProperties.PREFIX)
@Getter
@Setter
public class EcsDiscoveryProperties {
    public static final String PREFIX = "spring.cloud.ecs.discovery";
    private String namespace;
    private String namespaceId;
    private boolean enabled = true;
    private Integer defaultPort = 8080;
    private Map<String, Integer> servicePorts = new HashMap<>();
    private List<String> includeServices = new ArrayList<>();
}
