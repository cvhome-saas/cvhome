package com.asrevo.cvhome.gateway.config.ssl;

import com.asrevo.cvhome.gateway.service.AcmService;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import reactor.netty.http.Http2SslContextSpec;

import java.security.cert.CertificateFactory;
import java.util.Optional;
import java.util.function.Supplier;

import static org.springframework.http.HttpMethod.POST;


@Configuration
@Slf4j
public class SslNettyConfig {
    private static Supplier<SslContext> getSslContextSupplier(String domain) {
        return () -> {
            try {
                log.warn("will create self signed certificates for domain {} ", domain);
                SelfSignedCertificate certificate = new SelfSignedCertificate(domain);
                Http2SslContextSpec sslContextSpec = Http2SslContextSpec.forServer(certificate.certificate(), certificate.privateKey());
                return sslContextSpec.sslContext();
            } catch (Exception e) {
                log.error("did not manged to create createSelfSsl for " + domain);
                return null;
            }
        };
    }

    @Bean
    public Supplier<SslContext> defaultSslContextSupplier(RestTemplate template, SslProperties sslProperties, CertificateFactory cf) {
        return () -> {
            String domain = sslProperties.getDefaultDomain();
            ParameterizedTypeReference<byte[]> responseType = new ParameterizedTypeReference<>() {
            };
            AcmService acmService = (d, fileType) -> {
                String url = "http://localhost:8082/api/v1/acm/domain-certificate-file?domain=" + d + "&fileType=" + fileType.name();
                return template.exchange(url, POST, null, responseType);
            };
            SslContext sslContext = acmService.getSslContext(cf, domain);
            return Optional.ofNullable(sslContext).orElseGet(getSslContextSupplier(domain));
        };
    }

    @SneakyThrows
    @Bean
    public CertificateFactory cf() {
        return CertificateFactory.getInstance("X.509");
    }

    @Bean
    public NettyServerCustomizer customizer(SSlProviderLoader slProviderLoader, Supplier<SslContext> defaultSslContextSupplier, SslProperties sslProperties) {
        SSlProviderLoader loader = new SSlProviderCacheLoader(slProviderLoader);
        return new DynamicSslLoaderNettyCustomizer(defaultSslContextSupplier, loader, sslProperties);
    }

    @SneakyThrows
    @Bean
    SSlProviderLoader slProviderLoader(AcmService acmService, CertificateFactory cf) {
        return new AcmCertificateLoaderImpl(acmService, cf);
    }

}