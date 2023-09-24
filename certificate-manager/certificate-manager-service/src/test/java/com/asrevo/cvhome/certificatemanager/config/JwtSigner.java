package com.asrevo.cvhome.certificatemanager.config;

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

@Component
public class JwtSigner {
    private final RSAKey rsaKey;
    private final PrivateKey rsaPrivateKey;

    public JwtSigner(RSAKey rsaKey) throws JOSEException {
        this.rsaKey = rsaKey;
        this.rsaPrivateKey = rsaKey.toRSAPrivateKey();
    }

    /*
        *
        * {
          "exp": 1695492609,
          "iat": 1695492309,
          "jti": "84a4b2a4-b463-4df4-9640-c669124402d3",       **************************
          "iss": "http://auth.gateway.com:9999/realms/cvhome",
          "aud": "account",
          "sub": "fcad941c-aea5-426f-8f17-428add82aeb1",
          "typ": "Bearer",               *****************************
          "azp": "microservice-app",                 ****************************
          "session_state": "ea59acba-c673-42f5-a374-5e0a30802683",         *****************
          "acr": "1",        ********************
          "allowed-origins": [
            "/*"
          ],                      ********************
          "realm_access": {
            "roles": [
              "ROLE_MICROSERVICE",
              "offline_access",
              "uma_authorization",
              "default-roles-cvhome",
              "ROLE_MICROSERVICE_GATEWAY"
            ]
          },            *************************
          "resource_access": {
            "account": {
              "roles": [
                "manage-account",
                "manage-account-links",
                "view-profile"
              ]
            }
          },            ******************
          "scope": "profile email",
          "sid": "ea59acba-c673-42f5-a374-5e0a30802683",
          "email_verified": false,
          "preferred_username": "microservice-gateway",
          "given_name": "",
          "family_name": ""
        }
        * */
    public String createJwt() throws JOSEException {

        var header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(rsaKey.getKeyID())
                .build();
        var payload = new JWTClaimsSet.Builder()
                .issueTime(Date.from(Instant.now()))
                .claim("jti", "84a4b2a4-b463-4df4-9640-c669124402d3")
                .issuer("http://auth.gateway.com:9999/realms/cvhome")
                .audience("account")
                .subject("fcad941c-aea5-426f-8f17-428add82aeb1")
                .claim("typ", "Bearer")
                .claim("azp", "microservice-app")
                .claim("scope", "profile email")
                .claim("sid", "ea59acba-c673-42f5-a374-5e0a30802683")
                .claim("email_verified", "false")
                .claim("preferred_username", "microservice-gateway")
                .claim("given_name", "")
                .claim("family_name", "")
                .build();

        var signedJWT = new SignedJWT(header, payload);
        signedJWT.sign(new RSASSASigner(rsaPrivateKey));
        return signedJWT.serialize();
    }


}