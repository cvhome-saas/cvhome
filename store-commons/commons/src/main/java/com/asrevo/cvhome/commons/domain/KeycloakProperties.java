package com.asrevo.cvhome.commons.domain;

public record KeycloakProperties(
        String serverUrl, String clientId, String clientSecret, String realm) {}
