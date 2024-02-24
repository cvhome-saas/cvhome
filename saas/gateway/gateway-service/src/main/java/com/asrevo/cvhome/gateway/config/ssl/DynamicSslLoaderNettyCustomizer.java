package com.asrevo.cvhome.gateway.config.ssl;

import io.netty.util.AsyncMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.SslProvider;

import java.util.Optional;
import java.util.function.Consumer;


@Slf4j
public class DynamicSslLoaderNettyCustomizer implements NettyServerCustomizer {

    private final ResolverSSlProviderCacheLoader slProviderLoader;
    private final AsyncMapping<String, SslProvider> asyncMapping;
    private final SslNettyLogFormatter accessLogFactory = new SslNettyLogFormatter();
    private final boolean redirectHttpToHttps = true;
    private final boolean allowAccessLog = true;

    private final HttpProtocol[] supportedProtocol = {HttpProtocol.HTTP11, HttpProtocol.H2};


    public DynamicSslLoaderNettyCustomizer(ResolverSSlProviderCacheLoader slProviderLoader) {
        this.slProviderLoader = slProviderLoader;
        this.asyncMapping = (domain, promise) ->
                // @formatter:off
                Optional.ofNullable(slProviderLoader.load(domain))
                        .map(promise::setSuccess)
                        .orElseGet(() -> promise.setFailure(new Error("invalid host " + domain)));
        // @formatter:on
    }


    @Override
    public HttpServer apply(HttpServer httpServer) {
        // @formatter:off
        return httpServer
                .accessLog(allowAccessLog, accessLogFactory)
                .secure(getSslContextSpec(), redirectHttpToHttps)
                .protocol(supportedProtocol);
        // @formatter:on

    }

    private Consumer<SslProvider.SslContextSpec> getSslContextSpec() {
        // @formatter:off
        return sslContextSpec -> sslContextSpec
                .sslContext(this.slProviderLoader.getDefaultSslContext())
                .setSniAsyncMappings(this.asyncMapping);
        // @formatter:on
    }
}
