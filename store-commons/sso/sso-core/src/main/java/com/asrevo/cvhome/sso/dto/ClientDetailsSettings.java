package com.asrevo.cvhome.sso.dto;

import java.util.Map;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;

public record ClientDetailsSettings(boolean requireProofKey, boolean requireAuthorizationConsent, String jwkSetUrl,
                                    SignatureAlgorithm tokenEndpointAuthenticationSigningAlgorithm, String x509CertificateSubjectDN,
                                    Map<String, Object> customSettings) {

}
