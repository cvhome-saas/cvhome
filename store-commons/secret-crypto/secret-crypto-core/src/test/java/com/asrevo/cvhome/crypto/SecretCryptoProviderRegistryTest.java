package com.asrevo.cvhome.crypto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Why switching the active crypto provider does not strand every secret encrypted under the previous one.
 *
 * <p>
 * Encryption always goes through the single active provider, but decryption is routed by the algorithm recorded in
 * the envelope itself. That asymmetry is the whole point: rotating to KMS has to leave the AES rows readable, and
 * the registry is the only thing that guarantees it.
 * </p>
 */
class SecretCryptoProviderRegistryTest {

    private static final String AES = "AES-256-GCM";

    private static final String KMS = "AWS-KMS";

    private static final byte[] PLAINTEXT = "secret".getBytes(StandardCharsets.UTF_8);

    private static SecretCryptoProvider providerOf(String id) {
        SecretCryptoProvider provider = mock(SecretCryptoProvider.class);
        when(provider.providerId()).thenReturn(id);
        return provider;
    }

    @Test
    void decryptGoesToTheProviderThatEncryptedTheValueNotTheActiveOne() {
        SecretCryptoProvider aes = providerOf(AES);
        SecretCryptoProvider kms = providerOf(KMS);
        SecretCryptoProviderRegistry registry = new SecretCryptoProviderRegistry(List.of(aes, kms), kms);

        registry.decrypt(EncryptedValue.builder().algorithm(AES).build());

        verify(aes).decrypt(any(EncryptedValue.class));
        verify(kms, never()).decrypt(any(EncryptedValue.class));
    }

    @Test
    void encryptAlwaysGoesToTheActiveProvider() {
        SecretCryptoProvider aes = providerOf(AES);
        SecretCryptoProvider kms = providerOf(KMS);
        SecretCryptoProviderRegistry registry = new SecretCryptoProviderRegistry(List.of(aes, kms), kms);

        registry.encrypt(PLAINTEXT);

        verify(kms).encrypt(PLAINTEXT);
        verify(aes, never()).encrypt(any(byte[].class));
    }

    @Test
    void anEnvelopeNamingAnUnregisteredAlgorithmFailsLoudlyRatherThanDecryptingWithTheWrongKey() {
        SecretCryptoProvider aes = providerOf(AES);
        SecretCryptoProviderRegistry registry = new SecretCryptoProviderRegistry(List.of(aes), aes);

        assertThatThrownBy(() -> registry.decrypt(EncryptedValue.builder().algorithm(KMS).build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(KMS);
    }

    @Test
    void theRegistryReportsTheActiveProvidersId() {
        SecretCryptoProvider kms = providerOf(KMS);

        assertThat(new SecretCryptoProviderRegistry(List.of(kms), kms).providerId()).isEqualTo(KMS);
    }
}
