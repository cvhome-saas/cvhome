package com.asrevo.cvhome.gateway.config.ssl;

import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import reactor.netty.tcp.SslProvider;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class SslExpirePolicy implements Expiry<String, SslProvider> {
    private final Duration defaultDuration;
    private final long acmeTlsCacheTime = Duration.of(15, ChronoUnit.SECONDS).toNanos();

    public SslExpirePolicy(Duration defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    @Override
    public long expireAfterCreate(String key, SslProvider value, long currentTime) {
        if (value.getSslContext().applicationProtocolNegotiator().protocols().contains(TlsAlpn01Challenge.ACME_TLS_1_PROTOCOL)) {
            return acmeTlsCacheTime;
        } else {
            return defaultDuration.toNanos();
        }
    }

    @Override
    public long expireAfterUpdate(String key, SslProvider value, long currentTime, @NonNegative long currentDuration) {
        return Long.MAX_VALUE;
    }

    @Override
    public long expireAfterRead(String key, SslProvider value, long currentTime, @NonNegative long currentDuration) {
        return Long.MAX_VALUE;
    }
}
