package com.asrevo.cvhome.uaa.sdk.dto;

import java.util.Map;

public record ClientDetailsSettings(boolean requireProofKey, boolean requireAuthorizationConsent, String jwkSetUrl,
		String tokenEndpointAuthenticationSigningAlgorithm, String x509CertificateSubjectDN,
		Map<String, Object> customSettings) {
}
