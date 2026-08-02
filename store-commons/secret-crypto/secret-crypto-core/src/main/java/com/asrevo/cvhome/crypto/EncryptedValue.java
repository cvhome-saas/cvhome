package com.asrevo.cvhome.crypto;

import java.util.Base64;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EncryptedValue {
    private static final String SEPARATOR = ":";
    private static final String PREFIX = "ENC:";
    private final int version;
    private final String keyId;
    private final String algorithm;
    private final byte[] ciphertext;
    private final byte[] iv;

    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    public static EncryptedValue deserialize(String serialized) {
        if (serialized == null || serialized.isEmpty()) {
            return null;
        }
        if (!isEncrypted(serialized)) {
            throw new IllegalArgumentException(String.format("Value is not encrypted or missing prefix: %s", serialized));
        }
        String content = serialized.substring(PREFIX.length());
        String[] parts = content.split(SEPARATOR);
        if (parts.length != 5) {
            throw new IllegalArgumentException(
                    String.format("Invalid serialized EncryptedValue (expected 5 parts): %s", serialized));
        }
        return EncryptedValue.builder()
                .version(Integer.parseInt(parts[0]))
                .keyId(parts[1])
                .algorithm(parts[2])
                .iv(Base64.getDecoder().decode(parts[3]))
                .ciphertext(Base64.getDecoder().decode(parts[4]))
                .build();
    }

    public String serialize() {
        EncryptedValue value = this;
        String ciphertextB64 = Base64.getEncoder().encodeToString(value.getCiphertext());
        String ivB64 = Base64.getEncoder().encodeToString(value.getIv());
        return PREFIX + String.format("%d%s%s%s%s%s%s%s%s",
                value.getVersion(), SEPARATOR,
                value.getKeyId(), SEPARATOR,
                value.getAlgorithm(), SEPARATOR,
                ivB64, SEPARATOR,
                ciphertextB64);
    }
}
