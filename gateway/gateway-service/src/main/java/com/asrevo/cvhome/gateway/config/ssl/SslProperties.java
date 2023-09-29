package com.asrevo.cvhome.gateway.config.ssl;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("com.asrevo.cvhome.ssl")
public class SslProperties {
    private String defaultDomain;
    private String subDomainFallback;
}
