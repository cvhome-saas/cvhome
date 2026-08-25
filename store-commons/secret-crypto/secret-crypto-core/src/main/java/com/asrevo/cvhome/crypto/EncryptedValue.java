package com.asrevo.cvhome.crypto;

import java.util.Arrays;
import java.util.Base64;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EncryptedValue {
    private static final String SEPARATOR = ":";
    private static final String PREFIX = "ENC:";
    private static final int FIELD_COUNT = 5;
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
        // -1 keeps a trailing empty field, so an envelope whose provider derives its own IV still reads as 5 parts.
        String[] parts = content.split(SEPARATOR, -1);
        if (parts.length < FIELD_COUNT) {
            throw new IllegalArgumentException(
                    String.format("Invalid serialized EncryptedValue (expected %d parts): %s", FIELD_COUNT, serialized));
        }
        // The key id is the only field that may itself contain the separator: a KMS key id is an ARN
        // (arn:aws:kms:region:account:key/id), which a fixed five-way split tears apart. Version, algorithm and the
        // two base64 fields cannot contain one, so anything between the first and the last three is the key id.
        int algorithmAt = parts.length - 3;
        String keyId = String.join(SEPARATOR, Arrays.copyOfRange(parts, 1, algorithmAt));
        return EncryptedValue.builder()
                .version(Integer.parseInt(parts[0]))
                .keyId(keyId)
                .algorithm(parts[algorithmAt])
                .iv(Base64.getDecoder().decode(parts[algorithmAt + 1]))
                .ciphertext(Base64.getDecoder().decode(parts[algorithmAt + 2]))
                .build();
    }

    /**
     * The five-field record {@link #deserialize(String)} reads back.
     *
     * <p>
     * The IV field is emitted empty rather than omitted when the provider derives its own — AWS KMS does, and used
     * to produce an envelope that threw on serialize and, had it been written, carried four fields where the reader
     * demands five. Empty keeps the field count fixed, which is what the reader actually depends on.
     * </p>
     */
    public String serialize() {
        EncryptedValue value = this;
        String ciphertextB64 = Base64.getEncoder().encodeToString(value.getCiphertext());
        String ivB64 = value.getIv() == null ? "" : Base64.getEncoder().encodeToString(value.getIv());
        return String.format("%s%d%s%s%s%s%s%s%s%s",
                PREFIX, value.getVersion(), SEPARATOR,
                value.getKeyId(), SEPARATOR,
                value.getAlgorithm(), SEPARATOR,
                ivB64, SEPARATOR,
                ciphertextB64);
    }
}
