package com.asrevo.cvhome.crypto.caffeine;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The decorator that keeps a KMS round trip off the path of every request that reads the same secret.
 *
 * <p>
 * The cache is keyed by the envelope itself, so it works only because {@code EncryptedValue} is a Lombok
 * {@code @Data} whose equality compares the ciphertext array by value. Turning that class into a record, or letting
 * it fall back to identity equality, would leave the cache permanently missing and nothing else would notice.
 * </p>
 */
class CachingSecretCryptoProviderTest {

    private static final byte[] SECRET = "sk_live_51H".getBytes(StandardCharsets.UTF_8);

    private static final byte[] BLOB = "blob".getBytes(StandardCharsets.UTF_8);

    private static final byte[] OTHER_BLOB = "other".getBytes(StandardCharsets.UTF_8);

    private static final String AES = "AES-256-GCM";

    private SecretCryptoProvider delegate;

    private CachingSecretCryptoProvider provider;

    @BeforeEach
    void setUp() {
        delegate = mock(SecretCryptoProvider.class);
        provider = new CachingSecretCryptoProvider(delegate, Duration.ofMinutes(10));
    }

    private static EncryptedValue envelope(byte[] ciphertext) {
        return EncryptedValue.builder().version(1).keyId("default-key").algorithm(AES)
                .ciphertext(ciphertext).build();
    }

    @Test
    void aSecondReadOfAnEqualEnvelopeIsServedFromTheCache() {
        when(delegate.decrypt(any(EncryptedValue.class))).thenReturn(SECRET);

        assertThat(provider.decrypt(envelope(BLOB))).isEqualTo(SECRET);
        assertThat(provider.decrypt(envelope(BLOB))).isEqualTo(SECRET);

        verify(delegate, times(1)).decrypt(any(EncryptedValue.class));
    }

    @Test
    void aDifferentCiphertextIsADifferentKeyAndReachesTheDelegate() {
        when(delegate.decrypt(any(EncryptedValue.class))).thenReturn(SECRET);

        provider.decrypt(envelope(BLOB));
        provider.decrypt(envelope(OTHER_BLOB));

        verify(delegate, times(2)).decrypt(any(EncryptedValue.class));
    }

    @Test
    void encryptIsNeverCachedSoEveryCallGetsAFreshIv() {
        provider.encrypt(SECRET);
        provider.encrypt(SECRET);

        verify(delegate, times(2)).encrypt(SECRET);
    }

    @Test
    void aFailedDecryptIsNotRememberedAsAResult() {
        when(delegate.decrypt(any(EncryptedValue.class))).thenThrow(new IllegalStateException("kms down"));

        assertThatThrownBy(() -> provider.decrypt(envelope(BLOB))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> provider.decrypt(envelope(BLOB))).isInstanceOf(IllegalStateException.class);

        verify(delegate, times(2)).decrypt(any(EncryptedValue.class));
    }

    @Test
    void theProviderIdIsTheDelegatesSoCachingIsInvisibleToTheRegistry() {
        when(delegate.providerId()).thenReturn(AES);

        assertThat(provider.providerId()).isEqualTo(AES);
    }
}
