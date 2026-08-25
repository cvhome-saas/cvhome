package com.asrevo.cvhome.billing.config;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;
import com.asrevo.cvhome.s2s.model.StripeProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Resolves the provider credentials once, at start-up, decrypting whatever arrives encrypted.
 *
 * <p>
 * Both values may be configured either as plaintext or as a {@code secret-crypto} envelope
 * ({@code ENC:1:default-key:AES-256-GCM:…}). Encrypted is the intended form: the envelope is safe to keep in a config
 * file because the AES key it needs lives outside the repository — an environment variable, or
 * {@code ~/.cvhome/secret-crypto/keys} — so a checked-in envelope discloses nothing on its own. Plaintext still works,
 * because tenancy configures the same property that way and this must not break it, but it is logged as a
 * warning rather than accepted silently.
 * </p>
 *
 * <p>
 * Resolved once and held, rather than decrypted per call: these are read on every Stripe request and every webhook,
 * and a per-call decrypt would put the key through a cipher thousands of times a day for no benefit. Nothing here is
 * ever logged, and the accessors are deliberately the only way to reach the values.
 * </p>
 */
@Slf4j
@Component
public class StripeCredentials {

    private static final String API_KEY = "api key";

    private static final String WEBHOOK_KEY = "webhook signing key";

    private final String apiKey;

    private final String webhookSigningKey;

    public StripeCredentials(StripeProperties properties, SecretCryptoProvider cryptoProvider) {
        this.apiKey = resolve(properties.key(), cryptoProvider, API_KEY);
        this.webhookSigningKey = resolve(properties.webhookSigningKey(), cryptoProvider, WEBHOOK_KEY);
    }

    private static String resolve(String configured, SecretCryptoProvider cryptoProvider, String what) {
        if (configured == null || configured.isBlank()) {
            // Not fatal: the catalog and every read-only path work without Stripe, and refusing to start would make
            // a service that is mostly not about Stripe unbootable in an environment that has no credentials yet.
            log.warn("No Stripe {} configured — any call to Stripe will fail until one is set", what);
            return null;
        }
        if (!EncryptedValue.isEncrypted(configured)) {
            log.warn("Stripe {} is configured in plaintext; encrypt it with secret-crypto instead", what);
            return configured;
        }
        byte[] plaintext = cryptoProvider.decrypt(EncryptedValue.deserialize(configured));
        log.info("Stripe {} decrypted from its secret-crypto envelope", what);
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    /**
     * The secret key every Stripe call is made with. Passed per call through {@code RequestOptions} — never assigned
     * to the global {@code Stripe.apiKey}, which would make the credential process-wide state that no call site can
     * see or override.
     */
    public String apiKey() {
        return apiKey;
    }

    /**
     * The secret a webhook payload's signature is verified against. This is what authenticates Stripe to us: the
     * endpoint is public and holds no credential of Stripe's, so a payload that does not verify against this is not
     * from Stripe and is not read.
     */
    public String webhookSigningKey() {
        return webhookSigningKey;
    }

    public boolean configured() {
        return apiKey != null;
    }

}
