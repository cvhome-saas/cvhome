package com.asrevo.cvhome.gateway.config.ssl;

import com.asrevo.cvhome.gateway.service.AcmService;
import io.netty.handler.ssl.SslContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.cert.CertificateFactory;
import java.util.function.Supplier;


@Configuration
@Slf4j
public class SslNettyConfig {

    @Bean
    public NettyServerCustomizer customizer(SSlProviderLoader slProviderLoader, Supplier<SslContext> defaultSslContextSupplier) {
        SSlProviderLoader loader = new SSlProviderCacheLoader(slProviderLoader);
        return new DynamicSslLoaderNettyCustomizer(defaultSslContextSupplier, loader);
    }


    @SneakyThrows
    @Bean
    SSlProviderLoader slProviderLoader(AcmService acmService, CertificateFactory cf, SslProperties sslProperties) {
        return new AcmCertificateLoaderImpl(acmService, cf, sslProperties);
    }

}


