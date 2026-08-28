package com.asrevo.cvhome.crypto.local;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.crypto.EncryptedValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AES-256-GCM over a locally held key: the provider that actually protects merchant payment credentials on a
 * self-hosted deployment.
 *
 * <p>
 * Two properties matter more than the round trip. A missing key has to stop the provider at construction rather than
 * at the first secret it is asked to read, and a tampered ciphertext has to fail — GCM's authentication tag is the
 * only thing standing between an edited database row and a silently wrong decryption.
 * </p>
 */
class LocalAesCryptoProviderTest {

    private static final byte[] KEY = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);

    private static final byte[] OTHER_KEY = "fedcba9876543210fedcba9876543210".getBytes(StandardCharsets.UTF_8);

    private static final byte[] SECRET = "sk_live_51H".getBytes(StandardCharsets.UTF_8);

    private static final String DECRYPTION_FAILED = "Decryption failed";

    private static LocalAesCryptoProvider providerOn(byte[] key) {
        return new LocalAesCryptoProvider(new StaticKeyProvider(key));
    }

    @Test
    void aMissingKeyStopsTheProviderAtConstructionNotAtTheFirstSecret() {
        LocalKeyProvider empty = mock(LocalKeyProvider.class);
        when(empty.getKey()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new LocalAesCryptoProvider(empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Active key ID not found");
    }

    @Test
    void aSecretComesBackUnchangedThroughItsOwnProvider() {
        LocalAesCryptoProvider provider = providerOn(KEY);

        assertThat(provider.decrypt(provider.encrypt(SECRET))).isEqualTo(SECRET);
    }

    @Test
    void theSameSecretEncryptsDifferentlyEveryTimeBecauseTheIvIsFresh() {
        LocalAesCryptoProvider provider = providerOn(KEY);

        EncryptedValue first = provider.encrypt(SECRET);
        EncryptedValue second = provider.encrypt(SECRET);

        assertThat(first.getIv()).isNotEqualTo(second.getIv());
        assertThat(first.getCiphertext()).isNotEqualTo(second.getCiphertext());
    }

    @Test
    void theEnvelopeRecordsTheAlgorithmTheRegistryRoutesDecryptionBy() {
        EncryptedValue encrypted = providerOn(KEY).encrypt(SECRET);

        assertThat(encrypted.getAlgorithm()).isEqualTo(LocalAesCryptoProvider.PROVIDER_ID);
        assertThat(encrypted.getVersion()).isEqualTo(1);
        assertThat(encrypted.getIv()).hasSize(12);
    }

    @Test
    void aTamperedCiphertextIsRefusedRatherThanDecryptedToGarbage() {
        LocalAesCryptoProvider provider = providerOn(KEY);
        EncryptedValue encrypted = provider.encrypt(SECRET);
        encrypted.getCiphertext()[0] ^= 0x01;

        assertThatThrownBy(() -> provider.decrypt(encrypted))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(DECRYPTION_FAILED);
    }

    @Test
    void aSecretEncryptedUnderAnotherKeyDoesNotDecrypt() {
        EncryptedValue encrypted = providerOn(KEY).encrypt(SECRET);

        assertThatThrownBy(() -> providerOn(OTHER_KEY).decrypt(encrypted))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(DECRYPTION_FAILED);
    }

    @Test
    void aKeyThatDisappearsAfterConstructionFailsTheDecryptItWasNeededFor() {
        LocalKeyProvider vanishing = mock(LocalKeyProvider.class);
        when(vanishing.getKey()).thenReturn(Optional.of(KEY), Optional.of(KEY), Optional.empty());
        LocalAesCryptoProvider provider = new LocalAesCryptoProvider(vanishing);
        EncryptedValue encrypted = provider.encrypt(SECRET);

        assertThatThrownBy(() -> provider.decrypt(encrypted))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Key not found");
    }

    @Test
    void theProviderIdIsTheOneRecordedInEveryEnvelopeItProduces() {
        assertThat(providerOn(KEY).providerId()).isEqualTo("AES-256-GCM");
    }
}
