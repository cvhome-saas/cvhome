package com.asrevo.cvhome.sso.token;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * Reads an impersonation request off {@code POST /oauth2/token}.
 *
 * <p>
 * The grant is RFC 8693's token exchange with the extension Keycloak also uses: {@code subject_token} is the
 * <em>operator's</em> token and {@code requested_subject} names the account to act as, because the operator holds no
 * token for that account and the standard alone cannot say "give me one". Two cvhome parameters complete it —
 * {@code impersonation_store} and {@code impersonation_mode} — plus {@code reason}, which is required because an
 * impersonation nobody can review is the one that has to be switched off again.
 * </p>
 *
 * <p>
 * Registered ahead of Spring's own token-exchange converter (a custom converter is consulted first), so this one
 * answers for the grant and the built-in one never sees it. Anything but {@code grant_type=token-exchange} is
 * declined with {@code null}, which hands the request on to the next converter.
 * </p>
 */
public final class ImpersonationExchangeConverter implements AuthenticationConverter {

    public static final String REQUESTED_SUBJECT = "requested_subject";

    public static final String STORE = "impersonation_store";

    public static final String MODE = "impersonation_mode";

    public static final String REASON = "reason";

    public static final String SUBJECT_TOKEN = "subject_token";

    public static final String SUBJECT_TOKEN_TYPE = "subject_token_type";

    public static final String REQUESTED_TOKEN_TYPE = "requested_token_type";

    public static final String ACCESS_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:access_token";

    /** Every parameter this grant defines; the rest of the form travels as additional parameters. */
    private static final Set<String> OWN = Set.of(OAuth2ParameterNames.GRANT_TYPE, SUBJECT_TOKEN, SUBJECT_TOKEN_TYPE,
            REQUESTED_TOKEN_TYPE, REQUESTED_SUBJECT, STORE, MODE, REASON, OAuth2ParameterNames.SCOPE);

    private static final String RFC = "https://datatracker.ietf.org/doc/html/rfc8693#section-2.1";

    @Override
    public Authentication convert(HttpServletRequest request) {
        if (!AuthorizationGrantType.TOKEN_EXCHANGE.getValue().equals(request.getParameter(OAuth2ParameterNames.GRANT_TYPE))) {
            return null;
        }
        Authentication client = SecurityContextHolder.getContext().getAuthentication();
        if (!(client instanceof OAuth2ClientAuthenticationToken) || !client.isAuthenticated()) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        Map<String, String[]> form = request.getParameterMap();
        String subjectToken = single(form, SUBJECT_TOKEN);
        tokenTypeIsAccessToken(single(form, SUBJECT_TOKEN_TYPE), SUBJECT_TOKEN_TYPE);
        tokenTypeIsAccessToken(optional(form, REQUESTED_TOKEN_TYPE), REQUESTED_TOKEN_TYPE);
        String requestedSubject = single(form, REQUESTED_SUBJECT);
        String store = single(form, STORE);
        String mode = single(form, MODE);
        String reason = single(form, REASON);
        Set<String> scopes = scopes(optional(form, OAuth2ParameterNames.SCOPE));
        Map<String, Object> additional = new HashMap<>();
        form.forEach((key, values) -> {
            if (!OWN.contains(key)) {
                additional.put(key, values.length == 1 ? values[0] : List.of(values));
            }
        });
        return new ImpersonationExchangeAuthenticationToken(client, subjectToken, requestedSubject, store, mode, reason,
                scopes, additional);
    }

    private static String single(Map<String, String[]> form, String name) {
        String value = optional(form, name);
        if (value == null) {
            throw invalid(name);
        }
        return value;
    }

    private static String optional(Map<String, String[]> form, String name) {
        String[] values = form.get(name);
        if (values == null) {
            return null;
        }
        if (values.length != 1 || !StringUtils.hasText(values[0])) {
            throw invalid(name);
        }
        return values[0].trim();
    }

    private static void tokenTypeIsAccessToken(String value, String name) {
        if (value != null && !ACCESS_TOKEN_TYPE.equals(value)) {
            throw invalid(name);
        }
    }

    private static Set<String> scopes(String scope) {
        return scope == null ? Set.of() : new LinkedHashSet<>(Arrays.asList(scope.split(" ")));
    }

    private static OAuth2AuthenticationException invalid(String parameter) {
        return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                String.format("OAuth 2.0 parameter '%s' is missing, repeated or malformed.", parameter), RFC));
    }

}
