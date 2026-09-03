package com.asrevo.cvhome.uaa.idp;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * An OpenID Connect provider inside the test JVM: who it will say signed in, and the key it signs with. The
 * controller reads this; the test writes it.
 */
public final class StubIdp {

    /**
     * Under {@code /api/} on purpose: uaa's SPA fallback router answers every dotless path that is not {@code /api/},
     * {@code /oauth2/} or {@code /actuator} with index.html, and it is consulted before this controller's mapping.
     */
    public static final String PATH = "/api/stub-idp";

    public static final String CODE = "stub-code";

    public static final String ACCESS_TOKEN = "stub-access";

    /** The audience an id token carries when the token request did not name the client (basic auth). */
    public static final String CLIENT_ID = "uaa-test";

    private static final RSAKey KEY = generate();

    /** The person the next login is: mutable on purpose, one stub for every scenario. */
    private static volatile Map<String, Object> claims = Map.of();

    private static final Map<String, String> NONCES = new ConcurrentHashMap<>();

    private static final String SUB = "sub";

    private StubIdp() {
    }

    public static void willReturn(String subject, String email, boolean verified, String givenName, String familyName) {
        claims = Map.of(SUB, subject, "email", email, "email_verified", verified, "given_name", givenName,
                "family_name", familyName);
    }

    public static Map<String, Object> claims() {
        return claims;
    }

    public static void rememberNonce(String state, String nonce) {
        if (state != null && nonce != null) {
            NONCES.put(state, nonce);
        }
    }

    public static String nonceFor(String state) {
        return state == null ? null : NONCES.get(state);
    }

    public static JWKSet jwks() {
        return new JWKSet(KEY.toPublicJWK());
    }

    public static String idToken(String issuer, String audience, String nonce) {
        try {
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder().issuer(issuer).audience(audience)
                    .subject(String.valueOf(claims.get(SUB)))
                    .issueTime(Date.from(Instant.now())).expirationTime(Date.from(Instant.now().plusSeconds(300)));
            claims.forEach(builder::claim);
            if (nonce != null) {
                builder.claim("nonce", nonce);
            }
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KEY.getKeyID()).build(), builder.build());
            jwt.sign(new RSASSASigner(KEY));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private static RSAKey generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            var pair = generator.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) pair.getPublic()).privateKey(pair.getPrivate()).keyID("stub").build();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
