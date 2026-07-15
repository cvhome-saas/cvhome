package com.asrevo.cvhome.crypto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EncryptedValue {
    private final int version;
    private final String keyId;
    private final String algorithm;
    private final byte[] ciphertext;
    private final byte[] iv;
}
