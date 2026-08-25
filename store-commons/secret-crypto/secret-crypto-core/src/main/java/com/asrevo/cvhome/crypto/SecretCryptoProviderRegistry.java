package com.asrevo.cvhome.crypto;

import java.util.List;

/**
 * Dispatches {@link #decrypt(EncryptedValue)} to whichever registered provider's
 * {@link SecretCryptoProvider#providerId()} matches {@link EncryptedValue#getAlgorithm()},
 * so switching the active provider never breaks decrypting values encrypted under a
 * previously-active provider. {@link #encrypt(byte[])} always goes through a single
 * designated active provider.
 */
public class SecretCryptoProviderRegistry implements SecretCryptoProvider {

    private final List<SecretCryptoProvider> providers;
    private final SecretCryptoProvider activeProvider;

    public SecretCryptoProviderRegistry(List<SecretCryptoProvider> providers, SecretCryptoProvider activeProvider) {
        this.providers = providers;
        this.activeProvider = activeProvider;
    }

    @Override
    public EncryptedValue encrypt(byte[] plaintext) {
        return activeProvider.encrypt(plaintext);
    }

    @Override
    public byte[] decrypt(EncryptedValue encryptedValue) {
        return providers.stream()
                .filter(p -> p.providerId().equals(encryptedValue.getAlgorithm()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "No SecretCryptoProvider available to decrypt algorithm: %s", encryptedValue.getAlgorithm())))
                .decrypt(encryptedValue);
    }

    @Override
    public String providerId() {
        return activeProvider.providerId();
    }
}
