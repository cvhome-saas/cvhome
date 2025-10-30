package com.asrevo.cvhome.s2s.oauth2;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class RefreshTokenTokenResponseClient extends OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> {

	@Override
	HttpEntity<?> getRequestEntity(OAuth2RefreshTokenGrantRequest request) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", request.getClientRegistration().getClientId());
		map.add("client_secret", request.getClientRegistration().getClientSecret());
		map.add("grant_type", request.getGrantType().getValue());
		map.add("refresh_token", request.getRefreshToken().getTokenValue());
		return new HttpEntity<>(map, headers);
	}

}
