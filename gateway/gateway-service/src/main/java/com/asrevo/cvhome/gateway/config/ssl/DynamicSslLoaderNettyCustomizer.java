package com.asrevo.cvhome.gateway.config.ssl;

import com.google.common.net.InternetDomainName;
import io.netty.util.AsyncMapping;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.SslProvider;

import java.util.function.Consumer;
import java.util.function.Function;


@Slf4j
public class DynamicSslLoaderNettyCustomizer implements NettyServerCustomizer {

    private final AsyncMapping<String, SslProvider> asyncMapping;
    private final HttpProtocol[] supportedProtocol = {HttpProtocol.HTTP11, HttpProtocol.H2};
    private final DelegatedSslContext sslContext;


    public DynamicSslLoaderNettyCustomizer(DelegatedSslContext sslContext, SSlProviderLoader slProviderLoader, SslProperties sslProperties) {
        Function<String, String> keyResolver = getKeyResolver(sslProperties);
        this.sslContext = sslContext;
        SslProvider sslProvider = SslProvider.builder().sslContext(sslContext).build();
        this.asyncMapping = (domain, promise) -> getSslProvider(slProviderLoader, sslProperties, domain, promise, keyResolver, sslProvider);
    }


    private static Promise<SslProvider> getSslProvider(SSlProviderLoader slProviderLoader, SslProperties sslProperties, String domain, Promise<SslProvider> promise, Function<String, String> keyResolver, SslProvider sslProvider) {
        String resolvedDomain = keyResolver.apply(domain);
        if (resolvedDomain.equals(sslProperties.getDefaultDomain())) {
            return promise.setSuccess(sslProvider);
        }
        SslProvider load = slProviderLoader.load(domain);
        if (load != null) {
            return promise.setSuccess(load);
        } else {
            return promise.setFailure(new Error("invalid host " + domain));
        }
    }

    private static Function<String, String> getKeyResolver(SslProperties sslProperties) {
        return it -> {
            String ourDomain = "." + sslProperties.getDefaultDomain();
            if (!Utils.isValidInet4Address(it) || !InternetDomainName.isValid(it)) {
                return sslProperties.getDefaultDomain();
            } else if (it.endsWith(ourDomain)) {
                return sslProperties.getSubDomainFallback();
            } else {
                return it;
            }
        };
    }

    @Override
    public HttpServer apply(HttpServer httpServer) {
        boolean redirectHttpToHttps = true;
        boolean allowAccessLog = true;
        return httpServer
                .accessLog(allowAccessLog, new SslNettyLogFormatter())
                .secure(getSslContextSpec(), redirectHttpToHttps)
                .protocol(supportedProtocol);
    }

    private Consumer<SslProvider.SslContextSpec> getSslContextSpec() {
        return sslContextSpec -> sslContextSpec.sslContext(this.sslContext).setSniAsyncMappings(this.asyncMapping);
    }
}
