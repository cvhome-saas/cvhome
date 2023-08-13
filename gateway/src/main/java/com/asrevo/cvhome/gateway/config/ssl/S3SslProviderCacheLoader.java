package com.asrevo.cvhome.gateway.config.ssl;

import com.asrevo.cvhome.gateway.cache.CacheLoader;
import com.asrevo.cvhome.gateway.service.AcmService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import reactor.netty.tcp.SslProvider;


@Slf4j
public class S3SslProviderCacheLoader implements CacheLoader<String, SslProvider> {
    private final AcmService acmService;
    private final SSlProviderLoader certificateLoader;


    public S3SslProviderCacheLoader(AcmService acmService, SSlProviderLoader slProviderLoader) {
        this.acmService = acmService;
        this.certificateLoader = slProviderLoader;
    }

    @Override
    public @Nullable SslProvider load(String domain) {
        return certificateLoader.load(domain);
    }


}
