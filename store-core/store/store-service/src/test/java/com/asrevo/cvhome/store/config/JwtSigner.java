package com.asrevo.cvhome.store.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtSigner {
    private final RSAKey rsaKey;
    private final PrivateKey rsaPrivateKey;

    public JwtSigner(RSAKey rsaKey) throws JOSEException {
        this.rsaKey = rsaKey;
        this.rsaPrivateKey = rsaKey.toRSAPrivateKey();
    }

    public String createJwt(Map<String, Object> claims) throws JOSEException {
        JWSHeader header = new JWSHeader
                .Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(rsaKey.getKeyID())
                .build();
        JWTClaimsSet.Builder builder = new JWTClaimsSet
                .Builder()
                .issueTime(Date.from(Instant.now()));

        claims.forEach(builder::claim);

        var signedJWT = new SignedJWT(header, builder.build());
        signedJWT.sign(new RSASSASigner(rsaPrivateKey));
        return signedJWT.serialize();
    }


}
