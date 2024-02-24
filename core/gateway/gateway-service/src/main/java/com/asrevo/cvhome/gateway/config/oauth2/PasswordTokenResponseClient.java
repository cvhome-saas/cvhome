package com.asrevo.cvhome.gateway.config.oauth2;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class PasswordTokenResponseClient extends OAuth2AccessTokenResponseClient<OAuth2PasswordGrantRequest> {

    @Override
    HttpEntity<?> getRequestEntity(OAuth2PasswordGrantRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", request.getClientRegistration().getClientId());
        map.add("client_secret", request.getClientRegistration().getClientSecret());
        map.add("grant_type", request.getGrantType().getValue());
        map.add("username", request.getUsername());
        map.add("password", request.getPassword());
        return new HttpEntity<>(map, headers);
    }
}
