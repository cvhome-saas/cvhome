package com.asrevo.cvhome.gateway.config.ssl;

import com.asrevo.cvhome.gateway.cache.Cache;
import com.asrevo.cvhome.gateway.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.tcp.SslProvider;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
public class SSlProviderCacheLoader implements SSlProviderLoader {
    private final SSlProviderLoader loader;
    private final Cache<String, SslProvider> cache;

    SSlProviderCacheLoader(SSlProviderLoader loader) {
        this.loader = loader;
        this.cache = new CacheBuilder<String, SslProvider>() {
        }.loader(loader::load).expiry(new SslExpirePolicy(Duration.of(1, ChronoUnit.DAYS))).build();
    }


    @Override
    public SslProvider load(String key) {
        return this.cache.get(key);
    }

    @Override
    public SslProvider defaultSslProvider() {
        return this.cache.get(loader.getDefaultDomain());
    }

    @Override
    public String getDefaultDomain() {
        return loader.getDefaultDomain();
    }

}
