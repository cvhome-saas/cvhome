package com.asrevo.cvhome.crypto;

public interface SecretCryptoProvider {

    EncryptedValue encrypt(byte[] plaintext);

    byte[] decrypt(EncryptedValue encryptedValue);

    /**
     * Stable identifier for this provider, matched against {@link EncryptedValue#getAlgorithm()}
     * to route a decrypt request to the provider that originally encrypted the value.
     */
    String providerId();
}
