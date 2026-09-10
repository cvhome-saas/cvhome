package com.asrevo.cvhome.testsupport.arch;

/**
 * What a service's filter chain does with a request-mapped handler that carries no {@code @PreAuthorize}. The two
 * chains in this repository are mirror images of each other, so an ungated handler means a different thing on each
 * side, and {@link CvhomeArchitectureRules#handlersAreGatedOrDeclaredAnonymous} needs to know which one it is checking.
 */
public enum HandlerPolicy {

    /**
     * A store-pod service: {@code /api/*}{@code /private/**} is authenticated, everything else is {@code permitAll}.
     * An ungated handler outside {@code /private/} is anonymous and must be allow-listed; a {@code /private/} handler
     * must always carry a token, so listing one as anonymous is itself a violation.
     */
    POD,

    /**
     * A store-core service: {@code /api/v1/*}{@code /public/**} is {@code permitAll}, everything else is
     * authenticated. An ungated handler under {@code /public/} is anonymous and must be allow-listed; an ungated
     * handler anywhere else is reachable by any bearer from either issuer and must either gain a token or be
     * declared in the {@code authenticatedOnly} set when a bare session is the intended gate.
     */
    CORE

}
