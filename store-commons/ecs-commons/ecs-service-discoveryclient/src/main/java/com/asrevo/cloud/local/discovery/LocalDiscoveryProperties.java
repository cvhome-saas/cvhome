package com.asrevo.cloud.local.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ashraf
 * orgnaized local discovery configration properites
 */
@ConfigurationProperties(LocalDiscoveryProperties.PREFIX)
@Getter
@Setter
public class LocalDiscoveryProperties {
    /**
     * prefix for the LocalDiscoveryProperties
     */
    public static final String PREFIX = "spring.cloud.local.discovery";

    private boolean enabled = true;
    private Map<String, List<LocalInstance>> services = new HashMap<>();
}
