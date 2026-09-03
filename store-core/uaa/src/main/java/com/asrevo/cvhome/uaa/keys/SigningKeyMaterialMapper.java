package com.asrevo.cvhome.uaa.keys;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;
import com.asrevo.cvhome.uaa.domain.SigningKey;
import com.asrevo.cvhome.uaa.errors.SigningKeyUnusableException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;

import lombok.RequiredArgsConstructor;

/**
 * Between a Nimbus key pair and the row that stores it: public half plain, private half encrypted.
 *
 * <p>
 * The same shape cua's {@code SocialLoginConfigMapper} uses for a provider's app secret, and for the same reason: the
 * mapper is the one place that knows a column is an envelope, so nothing above it ever sees ciphertext and nothing
 * below it ever sees the key.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class SigningKeyMaterialMapper {

    private final SecretCryptoProvider crypto;

    public SigningKey toActiveEntity(RSAKey key, Instant now) {
        EncryptedValue envelope = crypto.encrypt(key.toJSONString().getBytes(StandardCharsets.UTF_8));
        return SigningKey.activate(key.getKeyID(), key.getAlgorithm().getName(), key.toPublicJWK().toJSONString(),
                envelope.serialize(), now);
    }

    /** The public half: always readable, because it was never encrypted. */
    public JWK toPublicJwk(SigningKey entity) throws SigningKeyUnusableException {
        try {
            return JWK.parse(entity.getPublicJwkJson());
        } catch (ParseException e) {
            throw SigningKeyUnusableException.of(entity.getKid(), e);
        }
    }

    /** The whole key, private half decrypted. Fails typed when the envelope cannot be opened. */
    public JWK toPrivateJwk(SigningKey entity) throws SigningKeyUnusableException {
        try {
            byte[] plain = crypto.decrypt(EncryptedValue.deserialize(entity.getPrivateJwkEnc()));
            return JWK.parse(new String(plain, StandardCharsets.UTF_8));
        } catch (ParseException | RuntimeException e) {
            throw SigningKeyUnusableException.of(entity.getKid(), e);
        }
    }

}
