package com.asrevo.cvhome.s2s.jwt;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "spring.security.oauth2.resourceserver.jwt")
@Getter
@Setter
public class IssuerUriSetConfigrationProperties {

    private Set<String> issuerUriSet;

}
