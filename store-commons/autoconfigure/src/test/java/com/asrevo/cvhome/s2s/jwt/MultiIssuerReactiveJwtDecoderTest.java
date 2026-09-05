package com.asrevo.cvhome.s2s.jwt;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * The gateway's decoder: the same contract as the servlet one, expressed as a {@link Mono}.
 *
 * <p>
 * The mapping arm is the whole point. A {@link BadJwtException} has to survive as itself, or the client's 401
 * becomes the gateway's 500 — and on the gateway that is every request in the estate, not one service's.
 * </p>
 */
class MultiIssuerReactiveJwtDecoderTest {

    private static final String REALM = "uaa";
    private static final String ISSUER = "https://uaa.gateway.com:9002";
    private static final String TOKEN = "a.b.c";
    private static final String JWKS_DOWN = "jwks unreachable";

    private static final String EXPIRED = "expired";

    private static IssuerRealm realm() {
        return new IssuerRealm(REALM, Set.of(ISSUER), "%s/oauth2/jwks".formatted(ISSUER), Set.of());
    }

    private static Jwt jwt() {
        return Jwt.withTokenValue(TOKEN).header("alg", "RS256").subject("s")
                .issuedAt(Instant.EPOCH).expiresAt(Instant.EPOCH.plusSeconds(3600)).build();
    }

    private static IssuerRegistry registryResolving() {
        IssuerRegistry registry = Mockito.mock(IssuerRegistry.class);
        when(registry.resolve(TOKEN)).thenReturn(realm());
        return registry;
    }

    @Test
    void adecoderIsBuiltOncePerRealmAndReusedAfterwards() {
        ReactiveJwtDecoder delegate = Mockito.mock(ReactiveJwtDecoder.class);
        when(delegate.decode(TOKEN)).thenReturn(Mono.just(jwt()));
        AtomicInteger built = new AtomicInteger();
        MultiIssuerReactiveJwtDecoder decoder = new MultiIssuerReactiveJwtDecoder(registryResolving(), r -> {
            built.incrementAndGet();
            return delegate;
        });

        StepVerifier.create(decoder.decode(TOKEN)).expectNextCount(1).verifyComplete();
        StepVerifier.create(decoder.decode(TOKEN)).expectNextCount(1).verifyComplete();

        assertThat(built.get()).isOne();
    }

    @Test
    void abadTokenSurvivesAsItselfSoTheClientKeepsItsFourOhOne() {
        ReactiveJwtDecoder delegate = Mockito.mock(ReactiveJwtDecoder.class);
        when(delegate.decode(TOKEN)).thenReturn(Mono.error(new BadJwtException(EXPIRED)));
        MultiIssuerReactiveJwtDecoder decoder =
                new MultiIssuerReactiveJwtDecoder(registryResolving(), r -> delegate);

        // On the gateway this arm is every request in the estate, not one service's.
        StepVerifier.create(decoder.decode(TOKEN))
                .expectErrorMatches(error -> error instanceof BadJwtException && EXPIRED.equals(error.getMessage()))
                .verify();
    }

    @Test
    void anythingElseBecomesOurOwnDecodeFailure() {
        MultiIssuerReactiveJwtDecoder decoder = new MultiIssuerReactiveJwtDecoder(registryResolving(), r -> {
            throw new IllegalStateException(JWKS_DOWN);
        });

        StepVerifier.create(decoder.decode(TOKEN))
                .expectErrorMatches(error -> error instanceof JwtException && !(error instanceof BadJwtException)
                        && error.getMessage().contains(JWKS_DOWN))
                .verify();
    }

    @Test
    void afactoryThatReturnsNothingIsReportedAsAConfigurationProblem() {
        MultiIssuerReactiveJwtDecoder decoder = new MultiIssuerReactiveJwtDecoder(registryResolving(), r -> null);

        StepVerifier.create(decoder.decode(TOKEN))
                .expectErrorMatches(error -> error instanceof JwtException
                        && error.getMessage().contains("factory configuration"))
                .verify();
    }

    @Test
    void aNullTokenOrCollaboratorIsRefusedAtOnce() {
        IssuerRegistry registry = Mockito.mock(IssuerRegistry.class);

        assertThatThrownBy(() -> new MultiIssuerReactiveJwtDecoder(null, r -> null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MultiIssuerReactiveJwtDecoder(registry, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MultiIssuerReactiveJwtDecoder(registry, r -> null).decode(null))
                .isInstanceOf(NullPointerException.class);
    }
}
