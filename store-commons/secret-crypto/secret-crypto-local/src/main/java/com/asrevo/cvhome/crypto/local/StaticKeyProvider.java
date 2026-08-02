package com.asrevo.cvhome.crypto.local;

import java.util.Optional;

/**
 * In-memory provider for keys. Useful for testing or simple configurations.
 */
public class StaticKeyProvider implements LocalKeyProvider {

    private final byte[] key;

    public StaticKeyProvider(byte[] key) {
        this.key = key;
    }

    @Override
    public Optional<byte[]> getKey() {
        return Optional.ofNullable(this.key);
    }
}
