package com.asrevo.cvhome.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("com.asrevo.cvhome.fargate")
public class FargateProperties {
    private String namespace;
}
