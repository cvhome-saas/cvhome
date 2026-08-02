package com.asrevo.cvhome.errors.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tuning for the shared error responses.
 *
 * @param typeBaseUri        prefix for the RFC-7807 {@code type} URI; the error code is appended in path form
 * @param includeDebugDetail when {@code true}, the root-cause message of an unclassified failure is included in the
 *                           response. Intended for local development only — leaving it on in production leaks internal structure to
 *                           clients, which is one of the problems this refactor exists to fix.
 */
@ConfigurationProperties("com.asrevo.cvhome.errors")
public record ErrorHandlingProperties(String typeBaseUri, boolean includeDebugDetail) {

    private static final String DEFAULT_TYPE_BASE_URI = "https://errors.asrevo.com";

    public ErrorHandlingProperties {
        typeBaseUri = typeBaseUri == null || typeBaseUri.isBlank() ? DEFAULT_TYPE_BASE_URI : typeBaseUri;
    }

}
