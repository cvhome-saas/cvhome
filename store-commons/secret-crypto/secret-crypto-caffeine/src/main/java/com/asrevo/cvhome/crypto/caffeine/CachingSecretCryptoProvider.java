package com.asrevo.cvhome.crypto.caffeine;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

public class CachingSecretCryptoProvider implements SecretCryptoProvider {

    private final SecretCryptoProvider delegate;
    private final Cache<EncryptedValue, byte[]> cache;

    public CachingSecretCryptoProvider(SecretCryptoProvider delegate, Duration duration) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(duration)
                .build();
    }

    @Override
    public EncryptedValue encrypt(byte[] plaintext) {
        return delegate.encrypt(plaintext);
    }

    @Override
    public byte[] decrypt(EncryptedValue encryptedValue) {
        return cache.get(encryptedValue, delegate::decrypt);
    }
}