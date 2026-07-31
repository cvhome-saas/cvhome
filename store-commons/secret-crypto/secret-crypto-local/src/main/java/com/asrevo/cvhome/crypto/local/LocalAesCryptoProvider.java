package com.asrevo.cvhome.crypto.local;

import java.security.SecureRandom;
import java.util.Map;

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

    private final LocalKeyProvider keyProvider;
    private final String activeKeyId;
    private final SecureRandom secureRandom = new SecureRandom();

    public LocalAesCryptoProvider(String activeKeyId, LocalKeyProvider keyProvider) {
        this.activeKeyId = activeKeyId;
        this.keyProvider = keyProvider;
        if (keyProvider.getKey(activeKeyId).isEmpty()) {
            throw new IllegalArgumentException("Active key ID not found in key provider");
        }
    }

    public LocalAesCryptoProvider(String activeKeyId, Map<String, byte[]> initialKeys) {
        this(activeKeyId, new StaticKeyProvider(initialKeys));
    }

    @Override
    public EncryptedValue encrypt(byte[] plaintext) {
        byte[] key = keyProvider.getKey(activeKeyId)
                .orElseThrow(() -> new IllegalStateException("Active key not found: " + activeKeyId));
        byte[] iv = new byte[IV_LENGTH_BYTE];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM), parameterSpec);
            byte[] ciphertext = cipher.doFinal(plaintext);

            return EncryptedValue.builder()
                    .version(1)
                    .keyId(activeKeyId)
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
        byte[] key = keyProvider.getKey(encryptedValue.getKeyId())
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
