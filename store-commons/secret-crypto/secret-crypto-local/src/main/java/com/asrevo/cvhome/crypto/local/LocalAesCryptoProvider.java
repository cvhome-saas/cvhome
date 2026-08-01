package com.asrevo.cvhome.crypto.local;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;

public class LocalAesCryptoProvider implements SecretCryptoProvider {

    public static final String PROVIDER_ID = "AES-256-GCM";

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final String KEY_ALGORITHM = "AES";
    private static final String ACTIVE_KEY_ID = "default-key";

    private final LocalKeyProvider keyProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public LocalAesCryptoProvider(LocalKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
        if (keyProvider.getKey().isEmpty()) {
            throw new IllegalArgumentException("Active key ID not found in key provider");
        }
    }

    @Override
    public EncryptedValue encrypt(byte[] plaintext) {
        byte[] key = keyProvider.getKey()
                .orElseThrow(() -> new IllegalStateException("Active key not found: " + ACTIVE_KEY_ID));
        byte[] iv = new byte[IV_LENGTH_BYTE];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM), parameterSpec);
            byte[] ciphertext = cipher.doFinal(plaintext);

            return EncryptedValue.builder()
                    .version(1)
                    .keyId(ACTIVE_KEY_ID)
                    .algorithm(PROVIDER_ID)
                    .ciphertext(ciphertext)
                    .iv(iv)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(EncryptedValue encryptedValue) {
        byte[] key = keyProvider.getKey()
                .orElseThrow(() -> new IllegalArgumentException("Key not found: " + encryptedValue.getKeyId()));

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, encryptedValue.getIv());
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM), parameterSpec);
            return cipher.doFinal(encryptedValue.getCiphertext());
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }
}
