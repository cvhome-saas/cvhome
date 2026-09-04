package com.asrevo.cvhome.s2s.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * How a service with several trusted issuers picks a decoder, and what each failure means.
 *
 * <p>
 * The two catch arms are the point. A {@link JwtException} from the delegate is the client's bad token and passes
 * through unchanged, so it stays a 401. Anything else — the JWKS endpoint unreachable, discovery failing — is ours
 * and becomes a generic decode failure, which the error contract renders as a 500. Collapsing the two would tell a
 * caller its token was bad when our own key server was down.
 * </p>
 */
class MultiIssuerJwtDecoderTest {

    private static final String UAA_REALM = "uaa";
    private static final String ISSUER = "https://uaa.gateway.com:9002";
    private static final String TOKEN = "a.b.c";
    private static final String ALG = "alg";
    private static final String RS256 = "RS256";
    private static final String SUBJECT = "s";
    private static final String EXPIRED = "expired";
    private static final String JWKS_DOWN = "jwks unreachable";

    private static IssuerRealm realm() {
        return new IssuerRealm(UAA_REALM, Set.of(ISSUER), "%s/oauth2/jwks".formatted(ISSUER), Set.of());
    }

    private static Jwt jwt(String issuer) {
        return Jwt.withTokenValue(TOKEN).header(ALG, RS256).subject(SUBJECT)
                .issuedAt(Instant.EPOCH).expiresAt(Instant.EPOCH.plusSeconds(3600))
                .claim(JwtClaimNames.ISS, issuer).build();
    }

    @Test
    void aDecoderIsBuiltOncePerRealmAndReusedAfterwards() {
        IssuerRegistry registry = Mockito.mock(IssuerRegistry.class);
        when(registry.resolve(TOKEN)).thenReturn(realm());
        JwtDecoder delegate = Mockito.mock(JwtDecoder.class);
        when(delegate.decode(TOKEN)).thenReturn(jwt(ISSUER));
        AtomicInteger built = new AtomicInteger();

        MultiIssuerJwtDecoder decoder = new MultiIssuerJwtDecoder(registry, r -> {
            built.incrementAndGet();
            return delegate;
        });

        assertThat(decoder.decode(TOKEN)).isNotNull();
        assertThat(decoder.decode(TOKEN)).isNotNull();
        // Rebuilding per request would re-fetch the JWKS on every call.
        assertThat(built.get()).isOne();
    }

    @Test
    void abadTokenFromTheDelegatePassesThroughSoItStaysAFourOhOne() {
        IssuerRegistry registry = Mockito.mock(IssuerRegistry.class);
        when(registry.resolve(TOKEN)).thenReturn(realm());
        JwtDecoder delegate = Mockito.mock(JwtDecoder.class);
        when(delegate.decode(TOKEN)).thenThrow(new BadJwtException(EXPIRED));

        MultiIssuerJwtDecoder decoder = new MultiIssuerJwtDecoder(registry, r -> delegate);

        assertThatThrownBy(() -> decoder.decode(TOKEN))
                .isInstanceOf(BadJwtException.class)
                .hasMessage(EXPIRED);
    }

    @Test
    void anUnreachableKeyServerIsOursNotTheCallersAndIsNotABadToken() {
        IssuerRegistry registry = Mockito.mock(IssuerRegistry.class);
        when(registry.resolve(TOKEN)).thenReturn(realm());

        MultiIssuerJwtDecoder decoder = new MultiIssuerJwtDecoder(registry, r -> {
            throw new IllegalStateException(JWKS_DOWN);
        });

        assertThatThrownBy(() -> decoder.decode(TOKEN))
                .isInstanceOf(JwtException.class)
                .isNotInstanceOf(BadJwtException.class)
                .hasMessageContaining(JWKS_DOWN);
    }

    @Test
    void afactoryThatReturnsNothingIsReportedAsAConfigurationProblem() {
        IssuerRegistry registry = Mockito.mock(IssuerRegistry.class);
        when(registry.resolve(TOKEN)).thenReturn(realm());

        MultiIssuerJwtDecoder decoder = new MultiIssuerJwtDecoder(registry, r -> null);

        assertThatThrownBy(() -> decoder.decode(TOKEN))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("factory configuration");
    }

    @Test
    void aNullTokenOrCollaboratorIsRefusedAtOnce() {
        IssuerRegistry registry = Mockito.mock(IssuerRegistry.class);

        assertThatThrownBy(() -> new MultiIssuerJwtDecoder(null, r -> null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MultiIssuerJwtDecoder(registry, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MultiIssuerJwtDecoder(registry, r -> null).decode(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void anIssuerInsideTheRealmValidatesAndAnythingElseDoesNot() {
        RealmIssuerValidator validator = new RealmIssuerValidator(realm());

        assertThat(validator.validate(jwt(ISSUER)).hasErrors()).isFalse();
        OAuth2TokenValidatorResult refused = validator.validate(jwt("https://elsewhere.example.com"));
        assertThat(refused.hasErrors()).isTrue();
        assertThat(refused.getErrors()).first()
                .extracting(error -> error.getDescription())
                .asString().contains("is not one of realm");
    }

    @Test
    void aTokenWithNoIssuerClaimIsRefusedRatherThanMatchingAnything() {
        RealmIssuerValidator validator = new RealmIssuerValidator(realm());

        assertThat(validator.validate(Jwt.withTokenValue(TOKEN).header(ALG, RS256)
                .claims(claims -> claims.putAll(Map.of("sub", SUBJECT)))
                .issuedAt(Instant.EPOCH).expiresAt(Instant.EPOCH.plusSeconds(1)).build()).hasErrors()).isTrue();
    }

    @Test
    void normalisationCoversCaseAndDefaultPortsButNotATrailingSlash() {
        RealmIssuerValidator validator = new RealmIssuerValidator(realm());

        // Case and an explicit default port are normalised away, so those forms match.
        assertThat(validator.validate(jwt("HTTPS://UAA.GATEWAY.COM:9002")).hasErrors()).isFalse();

        // A trailing slash is not: normalizeUri keeps the path, so "…:9002/" is a different issuer. That is why
        // IssuerRealm's contract is to register every form a server may present rather than to canonicalise one.
        assertThat(validator.validate(jwt("%s/".formatted(ISSUER))).hasErrors()).isTrue();
    }

    @Test
    void aNullRealmIsRefusedAtConstruction() {
        assertThatThrownBy(() -> new RealmIssuerValidator(null)).isInstanceOf(NullPointerException.class);
    }
}
