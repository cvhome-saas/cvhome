package com.asrevo.cvhome.crypto.local;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * Generates a random AES-256 key per key ID on first access and keeps it in memory for the
 * lifetime of this instance. Useful for local development/testing where persisting a key isn't
 * needed; encrypted values won't survive an application restart.
 */
public class RandomKeyProvider implements LocalKeyProvider {

    private static final int KEY_LENGTH_BYTES = 32;

    private static final SecureRandom secureRandom = new SecureRandom();
    private final byte[] key;

    public RandomKeyProvider() {
        this.key = createRandomKey();
    }

    public RandomKeyProvider(byte[] key) {
        this.key = key;
    }


    private static byte[] createRandomKey() {
        byte[] key = new byte[KEY_LENGTH_BYTES];
        secureRandom.nextBytes(key);
        return key;
    }

    @Override
    public Optional<byte[]> getKey() {
        return Optional.ofNullable(key);
    }
}
