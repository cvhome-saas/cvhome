package com.asrevo.cvhome.gateway.config.ssl;

import com.asrevo.cvhome.gateway.service.AcmService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class SslNettyConfig {

    @Bean
    public NettyServerCustomizer customizer(SSlProviderLoader slProviderLoader) {
        SSlProviderLoader loader = new SSlProviderCacheLoader(slProviderLoader);
        return new DynamicSslLoaderNettyCustomizer(loader);
    }

    @SneakyThrows
    @Bean
    SSlProviderLoader slProviderLoader(AcmService acmService, SslProperties sslProperties) {
        return new AcmCertificateLoaderImpl(acmService, sslProperties);
    }

}


