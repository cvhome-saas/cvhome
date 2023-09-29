package com.asrevo.cvhome.gateway.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateFileType;
import com.asrevo.cvhome.gateway.config.ssl.AcmeTls1SslContextSpec;
import io.netty.handler.ssl.SslContext;
import lombok.SneakyThrows;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.netty.http.Http2SslContextSpec;
import reactor.netty.tcp.SslProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public interface AcmService {
    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AcmService.class);

    ResponseEntity<byte[]> getDomainCertificateFile(String domain, CertificateFileType fileType);

    default ByteArrayInputStream getFile(String domainRequest, CertificateFileType fileType) {
        try {
            log.info("will get domain {} file {}", domainRequest, fileType.getType());

            ResponseEntity<byte[]> response = getDomainCertificateFile(domainRequest, fileType);

            if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
                return new ByteArrayInputStream(response.getBody());
            } else {
                log.warn("couldn't fetch domain {} file {}", domainRequest, fileType.getType());
                return null;
            }
        } catch (Exception e) {
            log.warn("error when fetch domain {} file {} message {}", domainRequest, fileType.getType(), e.getMessage());
            return null;
        }
    }


    @SneakyThrows
    default SslProvider getSslProvider(CertificateFactory certificateFactory, String domain) {
        SslContext sslContext = getSslContext(certificateFactory, domain);
        if (sslContext == null) {
            log.warn("couldn't get domain sslContext for domain {}", domain);
            return null;
        }
        return SslProvider.builder().sslContext(sslContext).build();
    }

    @SneakyThrows
    default SslContext getSslContext(CertificateFactory certificateFactory, String domain) {
        log.info("will getSslContext for domain {} ", domain);
        InputStream keyS3Object = getFile(domain, CertificateFileType.KEY);
        if (keyS3Object == null) return null;
        InputStream crtS3Object = getFile(domain, CertificateFileType.CRT);
        if (crtS3Object == null) return null;
        KeyPair keyPair = KeyPairUtils.readKeyPair(new InputStreamReader(keyS3Object));
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(crtS3Object);
        if (keyPair != null && cert != null) {
            if (cert.getIssuerX500Principal().getName().equals("CN=acme.invalid")) {
                log.info("will generate AcmeTls1 for domain {}", domain);
                return AcmeTls1SslContextSpec.forServer(keyPair.getPrivate(), cert).sslContext();
            } else {
                log.info("will generate Http2 for domain {}", domain);
                return Http2SslContextSpec.forServer(keyPair.getPrivate(), cert).sslContext();
            }

        }
        return null;

    }


}
