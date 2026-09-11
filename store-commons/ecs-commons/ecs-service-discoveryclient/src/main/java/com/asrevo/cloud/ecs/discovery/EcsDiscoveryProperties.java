package com.asrevo.cloud.ecs.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(EcsDiscoveryProperties.PREFIX)
@Getter
@Setter
public class EcsDiscoveryProperties {

    public static final String PREFIX = "spring.cloud.ecs.discovery";

    private String namespace;

    private String namespaceId;

    /**
     * Off unless a deployment turns it on — {@code fargate-config.yml} does. The bean used to be conditional on this
     * property being set at all, which made unset mean off whatever this default said.
     */
    private boolean enabled;

    private Integer defaultPort = 8080;

    private Map<String, Integer> servicePorts = new HashMap<>();

    private List<String> includeServices = new ArrayList<>();


    /**
     * Whether Cloud Map is consulted at all: switched on, and a namespace to look in. A blank namespace would query a
     * namespace named "" — which answers nothing and looks exactly like every service being down.
     */
    public boolean discoversFromCloudMap() {
        return enabled && StringUtils.hasText(namespace);
    }

}
