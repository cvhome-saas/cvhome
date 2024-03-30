package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.gateway.config.ssl.DynamicSslLoaderNettyCustomizer;
import com.asrevo.cvhome.gateway.config.ssl.ResolverSSlProviderCacheLoader;
import com.asrevo.cvhome.gateway.config.ssl.SSlProviderCacheLoader;
import com.asrevo.cvhome.gateway.config.ssl.StartupApplicationListener;
import com.asrevo.cvhome.gateway.service.AcmService;
import com.asrevo.cvhome.s2s.model.SaasProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class SslNettyConfig {
    @Bean
    public ResolverSSlProviderCacheLoader resolverSSlProviderCacheLoader(AcmService acmService, SaasProperties sslProperties) {
        SSlProviderCacheLoader cacheLoader = new SSlProviderCacheLoader(acmService);
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