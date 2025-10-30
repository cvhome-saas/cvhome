package com.asrevo.cvhome.s2s.jwt;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.oauth2.resourceserver.jwt")
@Getter
@Setter
public class IssuerUriSetConfigrationProperties {

	private Set<String> issuerUriSet;

}
