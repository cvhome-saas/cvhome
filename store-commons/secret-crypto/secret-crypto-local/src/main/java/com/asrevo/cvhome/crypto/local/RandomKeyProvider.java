package com.asrevo.cvhome.crypto.local;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates a random AES-256 key per key ID on first access and keeps it in memory for the
 * lifetime of this instance. Useful for local development/testing where persisting a key isn't
 * needed; encrypted values won't survive an application restart.
 */
public class RandomKeyProvider implements LocalKeyProvider {

    private static final int KEY_LENGTH_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, byte[]> keys = new ConcurrentHashMap<>();

    @Override
    public Optional<byte[]> getKey(String keyId) {
        return Optional.of(keys.computeIfAbsent(keyId, _ -> {
            byte[] key = new byte[KEY_LENGTH_BYTES];
            secureRandom.nextBytes(key);
            return key;
        }));
    }
}
