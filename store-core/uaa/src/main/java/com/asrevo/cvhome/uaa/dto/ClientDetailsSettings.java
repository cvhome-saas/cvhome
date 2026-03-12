package com.asrevo.cvhome.uaa.dto;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;

public record ClientDetailsSettings(boolean requireProofKey, boolean requireAuthorizationConsent, String jwkSetUrl,
		SignatureAlgorithm tokenEndpointAuthenticationSigningAlgorithm, String x509CertificateSubjectDN) {

}
