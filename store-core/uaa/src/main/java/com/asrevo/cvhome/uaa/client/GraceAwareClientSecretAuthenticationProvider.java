package com.asrevo.cvhome.uaa.client;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.ClientSecretAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import com.asrevo.cvhome.uaa.repo.ClientSecretHistoryRepository;

/**
 * Client-secret authentication that also accepts a secret rotated out within its grace window.
 *
 * <p>
 * Spring's {@link ClientSecretAuthenticationProvider} is {@code final} and matches only the one hash on the
 * registration. Rather than copy its expiry and PKCE handling, this provider decides <em>which hash</em> the client
 * should be matched against — the live one, or a retired one that is still inside its window — and then hands a
 * one-client view carrying that hash to a fresh instance of Spring's provider. Everything the stock provider checks,
 * it still checks; the only thing that changes is the row it reads.
 * </p>
 *
 * <p>
 * Registered by replacing the stock provider in the client-authentication configurer, so it sees exactly the
 * authentications the stock one would. <strong>Not a bean:</strong> Spring Security adopts a lone
 * {@code AuthenticationProvider} bean as the global authentication manager's provider, which would put this in front of
 * the form login and break it.
 * </p>
 */
public class GraceAwareClientSecretAuthenticationProvider implements AuthenticationProvider {

    private final RegisteredClientRepository clients;

    private final OAuth2AuthorizationService authorizations;

    private final PasswordEncoder encoder;

    private final ClientSecretHistoryRepository history;

    private final Clock clock;

    public GraceAwareClientSecretAuthenticationProvider(RegisteredClientRepository clients,
                                                        OAuth2AuthorizationService authorizations,
                                                        PasswordEncoder encoder, ClientSecretHistoryRepository history,
                                                        Clock clock) {
        this.clients = clients;
        this.authorizations = authorizations;
        this.encoder = encoder;
        this.history = history;
        this.clock = clock;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2ClientAuthenticationToken token = (OAuth2ClientAuthenticationToken) authentication;
        if (!isSecretMethod(token.getClientAuthenticationMethod())) {
            return null;
        }
        RegisteredClient client = clients.findByClientId(token.getPrincipal().toString());
        // The live registry unless a retired secret is what was presented; then a one-client view of that hash.
        RegisteredClientRepository view = client == null ? clients
                : graceView(client, token.getCredentials()).<RegisteredClientRepository>map(SingleClientRepository::new)
                        .orElse(clients);
        ClientSecretAuthenticationProvider stock = new ClientSecretAuthenticationProvider(view, authorizations);
        stock.setPasswordEncoder(encoder);
        return stock.authenticate(authentication);
    }

    /**
     * The client carrying a retired hash, when the presented secret is one still inside its grace window; empty when
     * the live secret (or nothing) was presented, so the stock provider reads the real registry.
     */
    private Optional<RegisteredClient> graceView(RegisteredClient client, Object credentials) {
        if (!(credentials instanceof String presented) || client.getClientSecret() == null
                || encoder.matches(presented, client.getClientSecret())) {
            return Optional.empty();
        }
        Instant now = clock.instant();
        return history.findByRegisteredClientIdAndRevokedAtIsNull(client.getId()).stream()
                .filter(row -> row.live(now))
                .filter(row -> encoder.matches(presented, row.getSecretHash()))
                .findFirst()
                .map(row -> RegisteredClient.from(client)
                        .clientSecret(row.getSecretHash())
                        // The retired hash's own window is what governs it, not the new secret's expiry.
                        .clientSecretExpiresAt(row.getExpiresAt())
                        .build());
    }

    private static boolean isSecretMethod(ClientAuthenticationMethod method) {
        return ClientAuthenticationMethod.CLIENT_SECRET_BASIC.equals(method)
                || ClientAuthenticationMethod.CLIENT_SECRET_POST.equals(method);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
