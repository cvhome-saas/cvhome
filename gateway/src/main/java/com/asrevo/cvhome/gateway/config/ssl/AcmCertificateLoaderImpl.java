package com.asrevo.cvhome.gateway.config.ssl;

import com.asrevo.cvhome.commons.domain.CertificateFileType;
import com.asrevo.cvhome.gateway.service.AcmService;
import com.google.common.net.InternetDomainName;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.netty.http.Http2SslContextSpec;
import reactor.netty.tcp.SslProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class AcmCertificateLoaderImpl implements SSlProviderLoader {
    private final AcmService acmService;
    private final SslProperties sslProperties;
    private final CertificateFactory cf = CertificateFactory.getInstance("X.509");
    private final Function<String, String> keyResolver;

    private static Function<String, String> getKeyResolver(SslProperties sslProperties) {
        return it -> {
            String ourDomain = "." + sslProperties.getDefaultDomain();
            if (it == null || it.endsWith(ourDomain) || Utils.isValidInet4Address(it) || !InternetDomainName.isValid(it) || "localhost".equals(it)) {
                return sslProperties.getSubDomainFallback();
            } else {
                return it;
            }
        };
    }


    public AcmCertificateLoaderImpl(AcmService acmService, SslProperties sslProperties) throws CertificateException {
        this.acmService = acmService;
        this.sslProperties = sslProperties;
        this.keyResolver = getKeyResolver(sslProperties);
    }

    @Override
    public SslProvider load(String key) {
        if (sslProperties.getDefaultDomain().equals(key)) {
            return defaultSslProvider();
        } else {
            return retrieveSslProvider(key);
        }
    }

    @SneakyThrows
    private SslProvider retrieveSslProvider(String key) {
        String domain = this.keyResolver.apply(key);
        log.info("will load domain " + domain);
        InputStream keyS3Object = this.getFile(domain, CertificateFileType.KEY);
        if (keyS3Object == null) return null;
        InputStream crtS3Object = this.getFile(domain, CertificateFileType.CRT);
        if (crtS3Object == null) return null;
        KeyPair keyPair = KeyPairUtils.readKeyPair(new InputStreamReader(keyS3Object));
        X509Certificate cert = (X509Certificate) cf.generateCertificate(crtS3Object);
        if (keyPair != null && cert != null) {
            log.info("done getting certificate from s3 for domain " + domain);
            if (cert.getIssuerX500Principal().getName().equals("CN=acme.invalid")) {
                AcmeTls1SslContextSpec sslContextSpec = AcmeTls1SslContextSpec.forServer(keyPair.getPrivate(), cert);
                return SslProvider.builder().sslContext(sslContextSpec.sslContext()).build();

            } else {
                Http2SslContextSpec sslContextSpec = Http2SslContextSpec.forServer(keyPair.getPrivate(), cert);
                return SslProvider.builder().sslContext(sslContextSpec.sslContext()).build();
            }

        }
        return null;
    }

    private static SslProvider createSelfSsl(String defaultDomain) {
        try {
            SelfSignedCertificate certificate = new SelfSignedCertificate(defaultDomain);
            Http2SslContextSpec sslContextSpec = Http2SslContextSpec.forServer(certificate.certificate(), certificate.privateKey());
            return SslProvider.builder().sslContext(sslContextSpec.sslContext()).build();
        } catch (Exception e) {
            log.error("did not manged to create createSelfSsl for " + defaultDomain);
            return null;
        }
    }

    @Override
    public SslProvider defaultSslProvider() {
        SslProvider loaded = this.retrieveSslProvider(sslProperties.getDefaultDomain());
        return Optional.ofNullable(loaded).orElseGet(() -> {
            log.warn("you do not have defaultSslProvider we will createSelfSsl to use it for domain {}", getDefaultDomain());
            return createSelfSsl(this.sslProperties.getDefaultDomain());
        });
    }

    @Override
    public String getDefaultDomain() {
        return sslProperties.getDefaultDomain();
    }

    private InputStream getFile(String domainRequest, CertificateFileType fileType) {
        Mono<ResponseEntity<byte[]>> certificateResponse = acmService.getDomainCertificateFile(domainRequest, fileType);
        try {
            ResponseEntity<byte[]> responseEntity = certificateResponse.toFuture().get();
            if (HttpStatus.OK.equals(responseEntity.getStatusCode()) && responseEntity.getBody() != null) {
                return new ByteArrayInputStream(responseEntity.getBody());
            } else {
                log.warn("did not get file because status is {} and body is nullable {}", responseEntity.getStatusCode().value(), Objects.isNull(responseEntity.getBody()));
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
