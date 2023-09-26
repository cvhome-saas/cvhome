package com.asrevo.cvhome.gateway.config.ssl;

import com.asrevo.cvhome.gateway.service.AcmService;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.tcp.SslProvider;

import java.security.cert.CertificateFactory;

@Slf4j
public class AcmCertificateLoaderImpl implements SSlProviderLoader {
    private final AcmService acmService;
    private final CertificateFactory certificateFactory;

    public AcmCertificateLoaderImpl(AcmService acmService, CertificateFactory certificateFactory) {
        this.certificateFactory = certificateFactory;
        this.acmService = acmService;
    }

    @Override
    public SslProvider load(String domain) {
        log.info("will load custom domain " + domain);
        return acmService.getSslProvider(certificateFactory, domain);
    }

}
