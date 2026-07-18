package com.asrevo.cvhome.s2s.model;

import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.asrevo.cvhome.uaa.oauth2")
public record OAuth2ClientProperties(Map<String, ClientInfo> clients) {
    public record ClientInfo(
            String secret,
            Set<String> redirectUriPaths,
            Set<String> postLogoutRedirectUriPaths,
            Set<String> scopes,
            Set<String> grantTypes
    ) {
    }
}
