package com.asrevo.cvhome.gateway.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.AsyncMapping;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.SslProvider;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SslNettyConfig {
    @Bean
    public NettyServerCustomizer customizer() {
        InMemorySslProviderAdapter loader = new InMemorySslProviderAdapterImpl();
        return new DynamicSslLoaderNettyCustomizer("localhost", loader);
    }
}

class DynamicSslLoaderNettyCustomizer implements NettyServerCustomizer {
    private final String defaultHost;
    private final InMemorySslProviderAdapter loader;

    public DynamicSslLoaderNettyCustomizer(String defaultHost, InMemorySslProviderAdapter loader) {
        this.defaultHost = defaultHost;
        this.loader = loader;

    }

    @Override
    public HttpServer apply(HttpServer httpServer) {
        SslContext defaultSslContext = loader.load(defaultHost).getSslContext();
        return httpServer
                .secure(sslContextSpec -> {
                    sslContextSpec.sslContext(defaultSslContext)
                            .setSniAsyncMappings(this.loader);
                });
    }
}

interface InMemorySslProviderAdapter extends AsyncMapping<String, SslProvider> {
    SslProvider load(String domain);
}

@Slf4j
class InMemorySslProviderAdapterImpl implements InMemorySslProviderAdapter {
    /**
     * in Memory SslContext contains SslProvider
     * you could improve it by adding support to expire before your certificates expire
     */
    private final Map<String, SslProvider> inMemorySslContext = new HashMap<>();

    @Override
    public SslProvider load(String domain) {
        return inMemorySslContext.computeIfAbsent(domain, this::getOrCreateHttp1SslContext);
    }


    /**
     * @param host from url or domain name
     * @return SslProvider
     * you should re-write this method to load certificate from centralized storage like s3
     * also you could add support to renew certificate and re load it again
     */
    private SslProvider getOrCreateHttp1SslContext(String host) {
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate(host);
            Http11SslContextSpec sslContextSpec = Http11SslContextSpec.forServer(ssc.certificate(), ssc.privateKey());
            log.info("generated self signed certificate for " + host);
            return SslProvider.builder().sslContext(sslContextSpec.sslContext()).build();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Future<SslProvider> map(String host, Promise<SslProvider> promise) {
        SslProvider load = load(host);
        if (load != null) {
            return promise.setSuccess(load);
        } else {
            return promise.setFailure(new Error("invalid host " + host));
        }
    }
}
