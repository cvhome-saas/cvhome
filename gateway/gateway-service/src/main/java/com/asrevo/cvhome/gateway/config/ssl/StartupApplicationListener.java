package com.asrevo.cvhome.gateway.config.ssl;

import com.asrevo.cvhome.gateway.service.AcmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.security.cert.CertificateFactory;


@Slf4j
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    private final AcmService acmService;
    private final DelegatedSslContext delegatedSslContext;
    private final CertificateFactory cf;
    private final SslProperties sslProperties;

    public StartupApplicationListener(AcmService acmService, DelegatedSslContext delegatedSslContext, CertificateFactory cf, SslProperties sslProperties) {
        this.acmService = acmService;
        this.delegatedSslContext = delegatedSslContext;
        this.cf = cf;
        this.sslProperties = sslProperties;
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        DelegatedSslContext sslContext = acmService.getSslContext(cf, sslProperties.getDefaultDomain());
        if (sslContext != null) {
            delegatedSslContext.setNewSslContext(sslContext);
        } else {
            log.warn("couldn't fetch default sslContext for {} will continue with initial one", sslProperties.getDefaultDomain());
        }
    }
}