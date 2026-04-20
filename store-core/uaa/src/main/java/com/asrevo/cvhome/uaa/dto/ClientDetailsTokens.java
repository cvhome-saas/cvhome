package com.asrevo.cvhome.uaa.dto;

import java.time.Duration;
import java.util.Map;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;

public record ClientDetailsTokens(Duration authorizationCodeTimeToLive, Duration accessTokenTimeToLive,
                                  OAuth2TokenFormat accessTokenFormat, Duration deviceCodeTimeToLive, boolean reuseRefreshTokens,
                                  Duration refreshTokenTimeToLive, SignatureAlgorithm idTokenSignatureAlgorithm,
                                  boolean x509CertificateBoundAccessTokens, Map<String, Object> customSettings) {

}
