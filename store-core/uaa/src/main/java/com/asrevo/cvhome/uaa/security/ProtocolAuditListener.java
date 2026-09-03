package com.asrevo.cvhome.uaa.security;

import java.time.Clock;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.audit.AuditActor;
import com.asrevo.cvhome.uaa.audit.AuditActorType;
import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.audit.AuditTargetType;
import com.asrevo.cvhome.uaa.domain.ClientExtension;
import com.asrevo.cvhome.uaa.repo.ClientExtensionRepository;

import lombok.RequiredArgsConstructor;

/**
 * The protocol's own events, as audit rows: a token issued, a token revoked, a client that failed to authenticate.
 *
 * <p>
 * These are the events no controller sees. The authorization server publishes them through the same
 * {@code AuthenticationEventPublisher} as the form login, so this listener sits beside
 * {@link AuthenticationAuditListener} and each ignores what the other handles: that one takes
 * {@code UsernamePasswordAuthenticationToken}, this one takes the OAuth2 tokens.
 * </p>
 *
 * <p>
 * Issuing a token also stamps the client's {@code last_token_issued_at}, which is what the clients list shows and
 * what makes an unused registration visible.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ProtocolAuditListener {

    static final String ANONYMOUS_CLIENT = "unknown";

    private final AuditService audit;

    private final ClientExtensionRepository extensions;

    private final Clock clock;

    @EventListener
    @Transactional
    public void onTokenIssued(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (!(authentication instanceof OAuth2AccessTokenAuthenticationToken token)) {
            return;
        }
        RegisteredClient client = token.getRegisteredClient();
        OAuth2AccessToken accessToken = token.getAccessToken();
        String scopes = accessToken.getScopes().isEmpty() ? "" : String.join(" ", sorted(accessToken.getScopes()));
        // The token's principal is the client for client_credentials and the person for an authorization code.
        String principal = token.getPrincipal() instanceof Authentication who ? who.getName() : null;
        audit.recordDetached(AuditRecord.of(AuditEventType.TOKEN_ISSUED)
                .actor(new AuditActor(AuditActorType.CLIENT, client.getId(), client.getClientId()))
                .client(client.getClientId())
                .target(AuditTargetType.TOKEN, null, principal)
                .detail(String.format("%s scopes=[%s] ttl=%ds", grantOf(token), scopes,
                        client.getTokenSettings().getAccessTokenTimeToLive().toSeconds())));
        ClientExtension extension = extensions.findById(client.getId())
                .orElseGet(() -> ClientExtension.create(client.getId(), null, clock.instant()));
        extension.setLastTokenIssuedAt(clock.instant());
        extension.setUpdatedAt(clock.instant());
        extensions.save(extension);
    }

    @EventListener
    public void onTokenRevoked(AuthenticationSuccessEvent event) {
        if (!(event.getAuthentication() instanceof OAuth2TokenRevocationAuthenticationToken revocation)) {
            return;
        }
        String clientId = revocation.getPrincipal() instanceof OAuth2ClientAuthenticationToken client
                && client.getRegisteredClient() != null ? client.getRegisteredClient().getClientId() : ANONYMOUS_CLIENT;
        audit.recordDetached(AuditRecord.of(AuditEventType.TOKEN_REVOKED)
                .actor(new AuditActor(AuditActorType.CLIENT, null, clientId))
                .client(clientId)
                .target(AuditTargetType.TOKEN, null, null)
                .detail("revocation endpoint"));
    }

    /**
     * A client that could not authenticate at the token endpoint: a wrong secret, an unknown client id, a disabled
     * one. The client id is what the caller claimed, which is exactly what an operator needs to see.
     */
    @EventListener
    public void onClientAuthFailure(AbstractAuthenticationFailureEvent event) {
        if (!(event.getAuthentication() instanceof OAuth2ClientAuthenticationToken client)) {
            return;
        }
        String clientId = client.getPrincipal() == null ? ANONYMOUS_CLIENT : String.valueOf(client.getPrincipal());
        audit.recordDetached(AuditRecord.of(AuditEventType.CLIENT_AUTH_FAILED)
                .actor(AuditActor.ANONYMOUS)
                .client(clientId)
                .target(AuditTargetType.CLIENT, null, clientId)
                .failed(reasonOf(event))
                .detail(String.valueOf(client.getClientAuthenticationMethod().getValue())));
    }

    private static String grantOf(OAuth2AccessTokenAuthenticationToken token) {
        return token.getRefreshToken() == null ? "grant=client_credentials" : "grant=authorization_code";
    }

    private static Set<String> sorted(Set<String> values) {
        return values.stream().sorted().collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private static String reasonOf(AbstractAuthenticationFailureEvent event) {
        return event.getException() instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauth
                ? oauth.getError().getErrorCode().toUpperCase(java.util.Locale.ROOT) : "INVALID_CLIENT";
    }

}
