package com.asrevo.cvhome.s2s.jwt;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.oauth2.jwt.BadJwtException;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The trust decision, and the two ways it used to get the answer wrong.
 *
 * <p>
 * <strong>The default port.</strong> A pod's cua issuer is built from an operator-entered endpoint in the pod
 * registry, so {@code https://host} and {@code https://host:443} both occur and mean the same server. The
 * configured side was normalized and the presented side was not, so the two never matched and every shopper
 * token was rejected. The {@code https}/{@code :443} half of this is the production case and cannot be
 * exercised locally, which is why it is a unit test and parameterised in both directions.
 * </p>
 *
 * <p>
 * <strong>The exception type.</strong> Everything the client can get wrong must be a {@link BadJwtException},
 * because Spring maps that to a 401 and maps a bare {@code JwtException} to an
 * {@code AuthenticationServiceException} — which escapes the filter chain as a 500. A stale token turning a
 * public product listing into an internal server error is what started all of this.
 * </p>
 */
class IssuerRegistryTest {

    private static final String SECRET = "a-test-signing-secret-long-enough-for-hmac-sha256";

    private static final String CUA = "cua";

    private static final String UAA = "uaa";

    private static final String CUA_ISSUER = "http://spg.gateway.com/cua";

    private static final String UAA_ISSUER = "http://uaa.gateway.com:8001";

    private static IssuerRegistry registryFor(String... issuerUris) {
        return new IssuerRegistry(List.of(new IssuerRealm(CUA, Set.of(issuerUris), null, Set.of())));
    }

    private static String tokenIssuedBy(String issuer) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder().subject("shopper")
                .expirationTime(new Date(System.currentTimeMillis() + 60_000));
        if (issuer != null) {
            claims.issuer(issuer);
        }
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        jwt.sign(new MACSigner(SECRET.getBytes()));
        return jwt.serialize();
    }

    @ParameterizedTest(name = "configured {0} accepts a token issued by {1}")
    @CsvSource({
            // http, both directions
            "http://spg.gateway.com/cua,       http://spg.gateway.com:80/cua",
            "http://spg.gateway.com:80/cua,    http://spg.gateway.com/cua",
            // https, both directions — the production axis
            "https://spg.example.com/cua,      https://spg.example.com:443/cua",
            "https://spg.example.com:443/cua,  https://spg.example.com/cua",
            // and the forms that are genuinely identical
            "https://spg.example.com/cua,      https://spg.example.com/cua",
    })
    void treatsADefaultPortAsAbsent(String configured, String presented) throws Exception {
        IssuerRealm realm = registryFor(configured).resolve(tokenIssuedBy(presented));

        assertThat(realm.name()).isEqualTo(CUA);
    }

    @Test
    void doesNotTreatANonDefaultPortAsAbsent() throws Exception {
        IssuerRegistry registry = registryFor(CUA_ISSUER);

        assertThatThrownBy(() -> registry.resolve(tokenIssuedBy("http://spg.gateway.com:1080/cua")))
                .isInstanceOf(BadJwtException.class);
    }

    @Test
    void rejectsAnUntrustedIssuerAsTheClientsFault() throws Exception {
        IssuerRegistry registry = registryFor(CUA_ISSUER);

        assertThatThrownBy(() -> registry.resolve(tokenIssuedBy("http://org1-store1.spg.gateway.com/cua")))
                .isInstanceOf(BadJwtException.class)
                .hasMessageContaining("Unsupported issuer");
    }

    @Test
    void rejectsAnUnparseableTokenAsTheClientsFault() {
        IssuerRegistry registry = registryFor(CUA_ISSUER);

        assertThatThrownBy(() -> registry.resolve("not-a-jwt"))
                .isInstanceOf(BadJwtException.class)
                .hasMessageContaining("Failed to parse token");
    }

    @Test
    void rejectsATokenWithNoIssuerAsTheClientsFault() throws Exception {
        IssuerRegistry registry = registryFor(CUA_ISSUER);
        String token = tokenIssuedBy(null);

        assertThatThrownBy(() -> registry.resolve(token))
                .isInstanceOf(BadJwtException.class)
                .hasMessageContaining("iss");
    }

    @Test
    void keepsRealmsApart() throws Exception {
        IssuerRegistry registry = new IssuerRegistry(List.of(
                new IssuerRealm(UAA, Set.of(UAA_ISSUER), null, Set.of()),
                new IssuerRealm(CUA, Set.of(CUA_ISSUER), null, Set.of("ROLE_CUSTOMER"))));

        assertThat(registry.resolve(tokenIssuedBy(UAA_ISSUER)).name()).isEqualTo(UAA);
        assertThat(registry.resolve(tokenIssuedBy("http://spg.gateway.com:80/cua")).name()).isEqualTo(CUA);
    }

    /** Two realms claiming one issuer is a configuration mistake with no safe reading, so it fails at startup. */
    @Test
    void refusesTwoRealmsClaimingTheSameIssuer() {
        List<IssuerRealm> realms = List.of(new IssuerRealm(UAA, Set.of(UAA_ISSUER), null, Set.of()),
                new IssuerRealm(CUA, Set.of(UAA_ISSUER), null, Set.of()));

        assertThatThrownBy(() -> new IssuerRegistry(realms)).isInstanceOf(IllegalArgumentException.class);
    }

}
