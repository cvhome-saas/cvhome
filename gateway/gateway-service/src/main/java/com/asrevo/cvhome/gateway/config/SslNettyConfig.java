package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.gateway.config.ssl.*;
import com.asrevo.cvhome.gateway.service.AcmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class SslNettyConfig {
    @Bean
    public ResolverSSlProviderCacheLoader resolverSSlProviderCacheLoader(AcmService acmService, SslProperties sslProperties) {
        SSlProviderCacheLoader cacheLoader = new SSlProviderCacheLoader(acmService::getSslProvider);
        return new ResolverSSlProviderCacheLoader(cacheLoader, sslProperties);
    }

    @Bean
    public NettyServerCustomizer nettyServerCustomizer(ResolverSSlProviderCacheLoader sslLoader) {
        return new DynamicSslLoaderNettyCustomizer(sslLoader);
    }

    @Bean
    public StartupApplicationListener applicationListener(ResolverSSlProviderCacheLoader sslLoader) {
        return new StartupApplicationListener(sslLoader);
    }
}