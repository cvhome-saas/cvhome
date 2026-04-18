package com.asrevo.cvhome.uaa.sdk.dto;

import java.time.Duration;
import java.util.Map;

public record ClientDetailsTokens(Duration authorizationCodeTimeToLive, Duration accessTokenTimeToLive,
                                  OAuth2TokenFormat accessTokenFormat, Duration deviceCodeTimeToLive, boolean reuseRefreshTokens,
                                  Duration refreshTokenTimeToLive, String idTokenSignatureAlgorithm,
                                  boolean x509CertificateBoundAccessTokens,
                                  Map<String, Object> customSettings) {
}
