package com.asrevo.cvhome.s2s.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Configuration
@Profile("signer")
public class ServletTestCustomSecurityConfig {
    @Configuration
    @Profile("signer")
    public static class SignerConfig {
        @Bean
        public RSAKey rsaKey() throws JOSEException {
            return new RSAKeyGenerator(2048)
                    .keyUse(KeyUse.SIGNATURE) // indicate the intended use of the key (optional)
                    .keyID(UUID.randomUUID().toString()) // give the key a unique ID (optional)
                    .generate();
        }

        @Bean
        public JwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
            return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        }
    }

    @Component
    @Profile("signer")
    public static class JwtSigner {
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

    @RestController
    @Profile("signer")
    public static class SignerController {
        private final JwtSigner jwtSigner;


        public SignerController(JwtSigner jwtSigner) {
            this.jwtSigner = jwtSigner;
        }

        @PostMapping("api/v1/test/sign")
        public Map<String, String> sign(@RequestBody Map<String, Object> claims) throws JOSEException {
            return Map.of("access_token", jwtSigner.createJwt(claims));
        }
    }

}