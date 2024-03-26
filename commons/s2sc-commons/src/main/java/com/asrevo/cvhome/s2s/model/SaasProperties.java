package com.asrevo.cvhome.s2s.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("com.asrevo.cvhome.saas")
public class SaasProperties {
    private String defaultDomain;
}
