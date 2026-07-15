package com.asrevo.cvhome.crypto.local;

import java.util.Optional;
import java.util.function.Function;

/**
 * Key provider that delegates to a custom function.
 */
public class CustomCallbackKeyProvider implements LocalKeyProvider {

    private final Function<String, byte[]> callback;

    public CustomCallbackKeyProvider(Function<String, byte[]> callback) {
        this.callback = callback;
    }

    @Override
    public Optional<byte[]> getKey(String keyId) {
        return Optional.ofNullable(callback.apply(keyId));
    }
}
