package com.asrevo.cvhome.uaa.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.domain.SigningKey;
import com.asrevo.cvhome.uaa.repo.SigningKeyRepository;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyPairService {

    private final SigningKeyRepository repo;

    /**
     * Returns a list of JWKs where the first is the active signing key and the rest are
     * previous keys for verification. If no key exists, an RSA key is generated, stored
     * as active, and returned.
     */
    @Transactional
    public List<JWK> getActiveAndPreviousKeys() {
        ensureActiveKeyExists();

        List<JWK> result = new ArrayList<>();
        repo.findTop5ByOrderByCreatedAtDesc().forEach(sk -> {
            try {
                JWK jwk = JWK.parse(sk.getJwkJson());
                // Only keep the first active key if there are multiple.
                if (sk.isActive()) {
                    boolean alreadyHasActive = result.stream().anyMatch(existing -> {
                        try {
                            return KeyUse.SIGNATURE.equals(existing.getKeyUse());
                        } catch (Exception _) {
                            return false;
                        }
                    });
                    if (alreadyHasActive) {
                        return;
                    }
                }
                result.add(jwk);
            } catch (Exception e) {
                // A key that cannot be parsed is a key whose tokens will fail verification; that must be visible.
                log.error("Signing key {} could not be parsed and is excluded from the JWK set", sk.getKid(), e);
            }
        });
        return result;
    }

    private void saveActive(RSAKey rsa) {
        SigningKey sk = new SigningKey();
        sk.setKid(rsa.getKeyID());
        sk.setActive(true);
        try {
            sk.setJwkJson(rsa.toJSONString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize JWK", e);
        }
        repo.save(sk);
    }

    private synchronized void ensureActiveKeyExists() {
        var activeList = repo.findAllActiveWithLock();
        if (activeList.isEmpty()) {
            log.info("No active signing key found, generating a new one...");
            RSAKey rsa = generateRsaJwk();
            saveActive(rsa);
        }
    }

    private RSAKey generateRsaJwk() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) kp.getPublic()).privateKey(kp.getPrivate())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyIDFromThumbprint()
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key", e);
        }
    }

}
