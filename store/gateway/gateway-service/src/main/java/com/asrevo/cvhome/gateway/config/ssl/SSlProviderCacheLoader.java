package com.asrevo.cvhome.gateway.config.ssl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.tcp.SslProvider;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
public class SSlProviderCacheLoader implements SSlProviderLoader {
    private final LoadingCache<String, SslProvider> cache;

    public SSlProviderCacheLoader(SSlProviderLoader loader) {
        this(loader, new SslExpirePolicy(Duration.of(1, ChronoUnit.DAYS)));
    }

    public SSlProviderCacheLoader(SSlProviderLoader loader, Expiry<String, SslProvider> expiry) {
        this.cache = Caffeine.newBuilder()
                .expireAfter(expiry)
                .build(loader::load);
    }

    @Override
    public SslProvider load(String key) {
        return this.cache.get(key);
    }

}
