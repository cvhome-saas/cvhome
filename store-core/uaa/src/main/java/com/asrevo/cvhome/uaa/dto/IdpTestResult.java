package com.asrevo.cvhome.uaa.dto;

/** What "Test" found: the endpoints answered, and for OIDC the issuer the discovery document names. */
public record IdpTestResult(boolean ok, String checked, String discoveredIssuer, String detail) {
}
