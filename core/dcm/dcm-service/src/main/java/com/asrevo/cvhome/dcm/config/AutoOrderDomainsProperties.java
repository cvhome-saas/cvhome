package com.asrevo.cvhome.dcm.config;

import com.asrevo.cvhome.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("com.asrevo.auto-order-domains")
@Getter
@Setter
public class AutoOrderDomainsProperties {
    private List<Domain> domains;
    private Domain defaultDomain;
}
