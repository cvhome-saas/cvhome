package com.asrevo.cvhome.gateway.config.ssl;

import com.asrevo.cvhome.gateway.service.AcmService;
import com.google.common.net.InternetDomainName;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.tcp.SslProvider;

import java.security.cert.CertificateFactory;
import java.util.function.Function;

@Slf4j
public class AcmCertificateLoaderImpl implements SSlProviderLoader {
    private final AcmService acmService;
    private final Function<String, String> keyResolver;
    private final CertificateFactory certificateFactory;

    public AcmCertificateLoaderImpl(AcmService acmService, CertificateFactory certificateFactory, SslProperties sslProperties) {
        this.certificateFactory = certificateFactory;
        this.acmService = acmService;
        this.keyResolver = getKeyResolver(sslProperties);
    }

    public static Function<String, String> getKeyResolver(SslProperties sslProperties) {
        return it -> {
            String ourDomain = "." + sslProperties.getDefaultDomain();
            if (it == null || it.endsWith(ourDomain) || Utils.isValidInet4Address(it) || !InternetDomainName.isValid(it) || "localhost".equals(it)) {
                return sslProperties.getSubDomainFallback();
            } else {
                return it;
            }
        };
    }

    @Override
    public SslProvider load(String key) {
        String domain = this.keyResolver.apply(key);
        log.info("will load custom domain " + domain);
        return acmService.getSslProvider(certificateFactory, domain);
    }

}
