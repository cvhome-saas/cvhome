package com.asrevo.cvhome.crypto.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Composites multiple LocalKeyProviders and returns the first key found.
 */
public class CompositeKeyProvider implements LocalKeyProvider {

    private final List<LocalKeyProvider> providers;

    public CompositeKeyProvider(List<LocalKeyProvider> providers) {
        this.providers = new ArrayList<>(providers);
    }

    @Override
    public Optional<byte[]> getKey(String keyId) {
        return providers.stream()
                .map(p -> p.getKey(keyId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
