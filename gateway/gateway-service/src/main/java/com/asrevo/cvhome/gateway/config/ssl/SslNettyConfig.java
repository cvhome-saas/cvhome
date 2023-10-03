package com.asrevo.cvhome.gateway.config.ssl;

import com.asrevo.cvhome.gateway.service.AcmService;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.Http2SslContextSpec;

import java.security.cert.CertificateFactory;


@Configuration
@Slf4j
public class SslNettyConfig {
    private static SslContext getSslContextSupplier(String domain) {
        try {
            log.info("will create initial self signed certificates for domain {} until we get a valid one", domain);
            SelfSignedCertificate certificate = new SelfSignedCertificate(domain);
            Http2SslContextSpec sslContextSpec = Http2SslContextSpec.forServer(certificate.certificate(), certificate.privateKey());
            return sslContextSpec.sslContext();
        } catch (Exception e) {
            log.error("did not manged to create createSelfSsl for " + domain);
            return null;
        }
    }


    @SneakyThrows
    @Bean
    public CertificateFactory cf() {
        return CertificateFactory.getInstance("X.509");
    }

    @Bean
    public DelegatedSslContext delegatedSslContext(SslProperties sslProperties) {
        return new DelegatedSslContext(getSslContextSupplier(sslProperties.getDefaultDomain()));
    }

    @Bean
    public NettyServerCustomizer customizer(SSlProviderLoader slProviderLoader, DelegatedSslContext sslContext, SslProperties sslProperties) {
        SSlProviderLoader loader = new SSlProviderCacheLoader(slProviderLoader);
        return new DynamicSslLoaderNettyCustomizer(sslContext, loader, sslProperties);
    }

    @Bean
    public StartupApplicationListener applicationListener(AcmService acmService, DelegatedSslContext delegatedSslContext, CertificateFactory cf, SslProperties sslProperties) {
        return new StartupApplicationListener(acmService, delegatedSslContext, cf, sslProperties);
    }
    @SneakyThrows
    @Bean
    SSlProviderLoader slProviderLoader(AcmService acmService, CertificateFactory cf) {
        return new AcmCertificateLoaderImpl(acmService, cf);
    }

}