package com.asrevo.cvhome.testsupport.security;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Mints RS256 JWTs with the test key the context's {@code JwtDecoder} trusts. Use {@link Tokens} for the claim
 * shapes the platform expects rather than calling this directly.
 */
public final class TestJwtSigner {

    private final RSAKey rsaKey;

    private final PrivateKey privateKey;

    public TestJwtSigner(RSAKey rsaKey) throws JOSEException {
        this.rsaKey = rsaKey;
        this.privateKey = rsaKey.toRSAPrivateKey();
    }

    public String sign(Map<String, Object> claims) {
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT)
                    .keyID(rsaKey.getKeyID())
                    .build();
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder().issueTime(Date.from(Instant.now()));
            claims.forEach(builder::claim);
            SignedJWT jwt = new SignedJWT(header, builder.build());
            jwt.sign(new RSASSASigner(privateKey));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("could not sign test JWT", e);
        }
    }

}
