package com.asrevo.cvhome.crypto.local;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory provider for keys. Useful for testing or simple configurations.
 */
public class StaticKeyProvider implements LocalKeyProvider {

    private final Map<String, byte[]> keys = new ConcurrentHashMap<>();

    public StaticKeyProvider(Map<String, byte[]> initialKeys) {
        if (initialKeys != null) {
            keys.putAll(initialKeys);
        }
    }

    public void addKey(String keyId, byte[] key) {
        keys.put(keyId, key);
    }

    @Override
    public Optional<byte[]> getKey(String keyId) {
        return Optional.ofNullable(keys.get(keyId));
    }
}
