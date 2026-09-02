package com.asrevo.cvhome.uaa.keys;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.uaa.domain.SigningKey;
import com.asrevo.cvhome.uaa.domain.SigningKeyStatus;
import com.asrevo.cvhome.uaa.errors.SigningKeyUnusableException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Public half plain, private half an envelope; a changed crypto key makes the private half unusable, not the public. */
class SigningKeyMaterialMapperTest {

    private static final byte KEY = 0x5a;

    private final FakeCrypto crypto = new FakeCrypto(KEY);

    private final SigningKeyMaterialMapper mapper = new SigningKeyMaterialMapper(crypto);

    static RSAKey rsa() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyRotationService.RSA);
        generator.initialize(KeyRotationService.RSA_BITS);
        var pair = generator.generateKeyPair();
        return new RSAKey.Builder((RSAPublicKey) pair.getPublic()).privateKey(pair.getPrivate()).keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256).keyIDFromThumbprint().build();
    }

    @Test
    void storesThePublicHalfPlainAndThePrivateHalfEncrypted() throws Exception {
        RSAKey rsa = rsa();

        SigningKey entity = mapper.toActiveEntity(rsa, Instant.EPOCH);

        assertThat(entity.getStatus()).isEqualTo(SigningKeyStatus.ACTIVE);
        assertThat(entity.getKid()).isEqualTo(rsa.getKeyID());
        assertThat(entity.getPublicJwkJson()).contains("\"n\"").doesNotContain("\"d\"");
        assertThat(EncryptedValue.isEncrypted(entity.getPrivateJwkEnc())).isTrue();
        assertThat(entity.getPrivateJwkEnc()).doesNotContain(rsa.getPrivateExponent().toString());
        assertThat(mapper.toPublicJwk(entity).isPrivate()).isFalse();
        assertThat(mapper.toPrivateJwk(entity).isPrivate()).isTrue();
        assertThat(mapper.toPrivateJwk(entity).getKeyID()).isEqualTo(rsa.getKeyID());
    }

    @Test
    void aChangedCryptoKeyMakesOnlyThePrivateHalfUnusable() throws Exception {
        SigningKey entity = mapper.toActiveEntity(rsa(), Instant.EPOCH);
        crypto.rekey((byte) 0x11);

        assertThatThrownBy(() -> mapper.toPrivateJwk(entity)).isInstanceOf(SigningKeyUnusableException.class);
        assertThat(mapper.toPublicJwk(entity).getKeyID()).isEqualTo(entity.getKid());
    }

}
