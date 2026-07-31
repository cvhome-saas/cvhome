package com.asrevo.cvhome.crypto.local;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Key provider that delegates to a custom function.
 */
public class CustomCallbackKeyProvider implements LocalKeyProvider {

    private final Supplier<byte[]> callback;

    public CustomCallbackKeyProvider(Supplier<byte[]> callback) {
        this.callback = callback;
    }

    @Override
    public Optional<byte[]> getKey() {
        return Optional.ofNullable(callback.get());
    }
}
