package com.asrevo.cvhome.sso.client;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.ClientSecretAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import com.asrevo.cvhome.sso.repo.ClientSecretHistoryRepository;

/**
 * Client-secret authentication that also accepts a secret rotated out within its grace window.
 *
 * <p>
 * Spring's {@link ClientSecretAuthenticationProvider} is {@code final} and matches only the one hash on the
 * registration. Rather than copy its expiry and PKCE handling, this provider lets it match the live registration
 * first and, only when that is refused as {@code invalid_client}, looks for a retired hash still inside its window and
 * hands a one-client view carrying that hash to a fresh instance of Spring's provider. Everything the stock provider
 * checks, it still checks; the only thing that changes is the row it reads. The live secret is hashed once per
 * request, which is what keeps the token endpoint at one bcrypt.
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
        // The live registry first, and only that: the stock provider loads the client and matches the hash itself, so
        // matching it here as well cost every token request a second bcrypt and a second client read — half of the
        // ~0.5 s the s2s token endpoint took. The grace path is tried only once the live secret has been refused.
        try {
            return stock(clients).authenticate(authentication);
        } catch (OAuth2AuthenticationException refused) {
            if (!OAuth2ErrorCodes.INVALID_CLIENT.equals(refused.getError().getErrorCode())) {
                throw refused;
            }
            RegisteredClient client = clients.findByClientId(token.getPrincipal().toString());
            RegisteredClient retired = client == null ? null : graceView(client, token.getCredentials()).orElse(null);
            if (retired == null) {
                throw refused;
            }
            return stock(new SingleClientRepository(retired)).authenticate(authentication);
        }
    }

    private ClientSecretAuthenticationProvider stock(RegisteredClientRepository view) {
        ClientSecretAuthenticationProvider stock = new ClientSecretAuthenticationProvider(view, authorizations);
        stock.setPasswordEncoder(encoder);
        return stock;
    }

    /**
     * The client carrying a retired hash, when the presented secret is one still inside its grace window; empty when
     * nothing retired matches, so the refusal of the live secret stands.
     */
    private Optional<RegisteredClient> graceView(RegisteredClient client, Object credentials) {
        if (!(credentials instanceof String presented)) {
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
