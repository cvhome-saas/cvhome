package com.asrevo.cvhome.sso.token;

import java.io.Serial;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import lombok.Getter;

/**
 * An impersonation request as it arrives at the token endpoint, before anything has been checked.
 *
 * <p>
 * Its own type, so that only {@link ImpersonationExchangeProvider} claims it: Spring's built-in token-exchange
 * provider supports its own token class and never sees this one.
 * </p>
 *
 * @param subjectToken     the operator's own access token
 * @param requestedSubject the account id to act as
 * @param store            the store to act in
 * @param mode             {@code read} or {@code write}, unparsed
 * @param reason           why — free text, required, and carried into the audit row
 * @param scopes           the scopes asked for; the provider narrows them to the client's
 */
@Getter
public class ImpersonationExchangeAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String subjectToken;

    private final String requestedSubject;

    private final String store;

    private final String mode;

    private final String reason;

    private final Set<String> scopes;

    public ImpersonationExchangeAuthenticationToken(Authentication clientPrincipal, String subjectToken,
                                                    String requestedSubject, String store, String mode, String reason,
                                                    Set<String> scopes, Map<String, Object> additionalParameters) {
        super(AuthorizationGrantType.TOKEN_EXCHANGE, clientPrincipal, additionalParameters);
        this.subjectToken = subjectToken;
        this.requestedSubject = requestedSubject;
        this.store = store;
        this.mode = mode;
        this.reason = reason;
        this.scopes = Set.copyOf(scopes);
    }

}
