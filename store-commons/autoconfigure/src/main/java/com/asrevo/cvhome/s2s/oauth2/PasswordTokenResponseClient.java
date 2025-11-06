package com.asrevo.cvhome.s2s.oauth2;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class PasswordTokenResponseClient extends OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

	@Override
	HttpEntity<?> getRequestEntity(OAuth2ClientCredentialsGrantRequest request) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", request.getClientRegistration().getClientId());
		map.add("client_secret", request.getClientRegistration().getClientSecret());
		map.add("grant_type", request.getGrantType().getValue());
		return new HttpEntity<>(map, headers);
	}

}
