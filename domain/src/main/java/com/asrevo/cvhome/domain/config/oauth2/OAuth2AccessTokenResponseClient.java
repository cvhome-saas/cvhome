package com.asrevo.cvhome.domain.config.oauth2;

import lombok.Setter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.core.endpoint.DefaultMapOAuth2AccessTokenResponseConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Setter
public abstract class OAuth2AccessTokenResponseClient<T extends AbstractOAuth2AuthorizationGrantRequest> {
    private DefaultMapOAuth2AccessTokenResponseConverter converter = new DefaultMapOAuth2AccessTokenResponseConverter();
    private ParameterizedTypeReference<HashMap<String, Object>> responseType = new ParameterizedTypeReference<>() {
    };
    protected RestTemplate restTemplate;

    public OAuth2AccessTokenResponseClient() {
        this.restTemplate = new RestTemplate();
    }

    public OAuth2AccessTokenResponse getTokenResponse(T request) {
        return exchange(request);
    }

    protected OAuth2AccessTokenResponse exchange(T request) {
        String tokenUri = request.getClientRegistration().getProviderDetails().getTokenUri();
        ResponseEntity<HashMap<String, Object>> exchange = restTemplate.exchange(tokenUri, HttpMethod.POST, getRequestEntity(request), responseType);
        if (exchange.getStatusCode() == HttpStatus.OK) {
            HashMap<String, Object> body = exchange.getBody();
            if (body != null) {
                return converter.convert(body);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    abstract HttpEntity<?> getRequestEntity(T request);

}
