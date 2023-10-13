package com.asrevo.cvhome.gateway.config.ssl;

import com.google.common.net.InternetDomainName;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.Http2SslContextSpec;
import reactor.netty.tcp.SslProvider;

@Slf4j
public class ResolverSSlProviderCacheLoader implements SSlProviderLoader {
    private final SSlProviderCacheLoader sSlProviderCacheLoader;
    private final SslProperties sslProperties;
    @Getter
    private final DelegatedSslContext defaultSslContext;
    @Getter
    private final SslProvider defaultSslProvider;

    public ResolverSSlProviderCacheLoader(SSlProviderCacheLoader sSlProviderCacheLoader, SslProperties sslProperties) {
        this.sSlProviderCacheLoader = sSlProviderCacheLoader;
        this.sslProperties = sslProperties;
        this.defaultSslContext = buildInitialiseContext();
        this.defaultSslProvider = SslProvider.builder().sslContext(this.defaultSslContext).build();
    }

    @Override
    public SslProvider load(String domain) {
        String resolvedDomain = resolveDomain(domain);
        SslProvider loaded = sSlProviderCacheLoader.load(resolvedDomain);
        if (loaded != null) {
            return loaded;
        } else if (sslProperties.getDefaultDomain().equals(resolvedDomain)) {
            return this.defaultSslProvider;
        } else if (sslProperties.getSubDomainFallback().equals(resolvedDomain)) {
            return this.defaultSslProvider;
        } else {
            return null;
        }
    }

    private String resolveDomain(String domain) {
        String ourDomain = "." + sslProperties.getDefaultDomain();
        if (Utils.isValidInet4Address(domain) || !InternetDomainName.isValid(domain)) {
            return sslProperties.getDefaultDomain();
        } else if (domain.endsWith(ourDomain)) {
            return sslProperties.getSubDomainFallback();
        } else {
            return domain;
        }
    }

    @SneakyThrows
    private DelegatedSslContext buildInitialiseContext() {
        log.info("will create initial self signed certificates for domain {} until we get a valid one", this.sslProperties.getDefaultDomain());
        SelfSignedCertificate certificate = new SelfSignedCertificate(this.sslProperties.getDefaultDomain());
        Http2SslContextSpec sslContextSpec = Http2SslContextSpec.forServer(certificate.certificate(), certificate.privateKey());
        return new DelegatedSslContext(sslContextSpec.sslContext(), false);
    }

    String defaultDomain() {
        return this.sslProperties.getDefaultDomain();
    }

    void requestNewSslContext() {
        SslProvider loaded = this.sSlProviderCacheLoader.load(this.defaultDomain());
        if (loaded != null && loaded.getSslContext() != null) {
            defaultSslContext.setNewSslContext(((DelegatedSslContext) loaded.getSslContext()));
        } else {
            log.warn("couldn't fetch default sslContext to refresh it for {} will continue with initial one", this.defaultDomain());
        }
    }
}
