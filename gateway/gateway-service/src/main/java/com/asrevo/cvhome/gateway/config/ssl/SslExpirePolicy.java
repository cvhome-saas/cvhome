package com.asrevo.cvhome.gateway.config.ssl;

import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import reactor.netty.tcp.SslProvider;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class SslExpirePolicy implements Expiry<String, SslProvider> {
    private final Duration defaultDuration;
    private final long acmeTlsCacheTime = Duration.of(15, ChronoUnit.SECONDS).toNanos();

    SslExpirePolicy(Duration defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    @Override
    public long expireAfterCreate(String key, SslProvider value, long currentTime) {
        if (((DelegatedSslContext) value.getSslContext()).isValidationCertificate()) {
            return acmeTlsCacheTime;
        } else {
            return defaultDuration.toNanos();
        }
    }

    @Override
    public long expireAfterUpdate(String key, SslProvider value, long currentTime, @NonNegative long currentDuration) {
        return defaultDuration.toNanos();
    }

    @Override
    public long expireAfterRead(String key, SslProvider value, long currentTime, @NonNegative long currentDuration) {
        return currentDuration;
    }
}
