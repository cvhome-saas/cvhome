package com.asrevo.cvhome.billing.config;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;
import com.asrevo.cvhome.s2s.model.StripeProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * How billing resolves its Stripe credentials, and why a missing one is not fatal.
 *
 * <p>
 * The catalog and every read-only path work without Stripe, so refusing to start would make a service that is
 * mostly not about Stripe unbootable in an environment that has no credentials yet. {@code configured()} is what
 * the call sites check instead. A plaintext value is accepted but warned about; an encrypted one is unwrapped
 * through secret-crypto, which is the only path a deployment should be using.
 * </p>
 */
class StripeCredentialsTest {

    private static final String PLAINTEXT_KEY = "sk_test_plain";
    private static final String DECRYPTED = "sk_live_decrypted";
    /** version:keyId:algorithm:iv:ciphertext, the five fields EncryptedValue.deserialize expects. */
    private static final String ENVELOPE = "ENC:1:k1:AES_GCM:aGVsbG8=:aGVsbG8=";

    private final SecretCryptoProvider crypto = Mockito.mock(SecretCryptoProvider.class);

    @Test
    void noCredentialsAtAllLeavesTheServiceBootableAndUnconfigured() {
        StripeCredentials credentials = new StripeCredentials(new StripeProperties(null, "  "), crypto);

        assertThat(credentials.apiKey()).isNull();
        assertThat(credentials.webhookSigningKey()).isNull();
        assertThat(credentials.configured()).isFalse();
        Mockito.verifyNoInteractions(crypto);
    }

    @Test
    void aPlaintextCredentialIsUsedAsIsAndCountsAsConfigured() {
        StripeCredentials credentials =
                new StripeCredentials(new StripeProperties(PLAINTEXT_KEY, PLAINTEXT_KEY), crypto);

        assertThat(credentials.apiKey()).isEqualTo(PLAINTEXT_KEY);
        assertThat(credentials.configured()).isTrue();
        Mockito.verifyNoInteractions(crypto);
    }

    @Test
    void anEncryptedCredentialIsUnwrappedThroughSecretCrypto() {
        assertThat(EncryptedValue.isEncrypted(ENVELOPE)).isTrue();
        when(crypto.decrypt(any())).thenReturn(DECRYPTED.getBytes(StandardCharsets.UTF_8));

        StripeCredentials credentials = new StripeCredentials(new StripeProperties(ENVELOPE, ENVELOPE), crypto);

        assertThat(credentials.apiKey()).isEqualTo(DECRYPTED);
        assertThat(credentials.webhookSigningKey()).isEqualTo(DECRYPTED);
        assertThat(credentials.configured()).isTrue();
    }

    @Test
    void theTwoCredentialsAreResolvedIndependently() {
        // A deployment part-way through encrypting its secrets has one of each, and both have to work.
        when(crypto.decrypt(any())).thenReturn(DECRYPTED.getBytes(StandardCharsets.UTF_8));

        StripeCredentials credentials = new StripeCredentials(new StripeProperties(PLAINTEXT_KEY, ENVELOPE), crypto);

        assertThat(credentials.apiKey()).isEqualTo(PLAINTEXT_KEY);
        assertThat(credentials.webhookSigningKey()).isEqualTo(DECRYPTED);
    }
}
