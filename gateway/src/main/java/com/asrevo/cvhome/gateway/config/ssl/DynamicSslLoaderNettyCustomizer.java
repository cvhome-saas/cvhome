package com.asrevo.cvhome.gateway.config.ssl;

import com.google.common.net.InternetDomainName;
import io.netty.handler.ssl.SslContext;
import io.netty.util.AsyncMapping;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.ConnectionInformation;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.logging.AccessLog;
import reactor.netty.http.server.logging.AccessLogFactory;
import reactor.netty.tcp.SslProvider;
import reactor.util.annotation.Nullable;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


@Slf4j
public class DynamicSslLoaderNettyCustomizer implements NettyServerCustomizer {
    static final String MISSING = "-";
    private static final String DEFAULT_LOG_FORMAT = "{} - {} [{}] \"{} {} {}\" {} {} {} {}";
    private static final AccessLogFactory accessLogFactory = args -> {
        ConnectionInformation connectionInformation = args.connectionInformation();
        String remoteAddress = MISSING;
        String requestHostName = MISSING;
        if (connectionInformation != null) {
            SocketAddress socketAddress = connectionInformation.remoteAddress();
            remoteAddress = applyAddress(socketAddress);
            requestHostName = connectionInformation.hostName();
        }
        String requestAccessDateTime =
                Optional.ofNullable(args.accessDateTime()).map(ZonedDateTime::toString).orElse(MISSING);
        CharSequence requestMethod = args.method();
        CharSequence requestUri = args.uri();
        String requestProtocol = args.protocol();
        CharSequence status = args.status();
        long requestDuration = args.duration();
        String contentLength = args.contentLength() > -1 ? String.valueOf(args.contentLength()) : MISSING;
        return AccessLog.create(DEFAULT_LOG_FORMAT, remoteAddress, args.user(), requestAccessDateTime, requestMethod,
                requestUri, requestProtocol, status, contentLength, requestDuration, requestHostName);
    };
    private final AsyncMapping<String, SslProvider> asyncMapping;
    private final HttpProtocol[] supportedProtocol = {HttpProtocol.HTTP11, HttpProtocol.H2};
    private final SslContext sslContext;


    public DynamicSslLoaderNettyCustomizer(Supplier<SslContext> defaultSslContextSupplier, SSlProviderLoader slProviderLoader, SslProperties sslProperties) {
        Function<String, String> keyResolver = getKeyResolver(sslProperties);
        this.sslContext = defaultSslContextSupplier.get();
        SslProvider sslProvider = SslProvider.builder().sslContext(sslContext).build();
        this.asyncMapping = (s, promise) -> getSslProviderPromise(slProviderLoader, sslProperties, s, promise, keyResolver, sslProvider);
    }

    static String applyAddress(@Nullable SocketAddress socketAddress) {
        return socketAddress instanceof InetSocketAddress ? ((InetSocketAddress) socketAddress).getHostString() :
                MISSING;
    }

    private static Promise<SslProvider> getSslProviderPromise(SSlProviderLoader slProviderLoader, SslProperties sslProperties, String s, Promise<SslProvider> promise, Function<String, String> keyResolver, SslProvider sslProvider) {
        String domain = keyResolver.apply(s);
        if (domain.equals(sslProperties.getDefaultDomain())) {
            return promise.setSuccess(sslProvider);
        }
        SslProvider load = slProviderLoader.load(s);
        if (load != null) {
            return promise.setSuccess(load);
        } else {
            return promise.setFailure(new Error("invalid host " + s));
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
                .accessLog(allowAccessLog, accessLogFactory)
                .secure(getSslContextSpecConsumer(), redirectHttpToHttps)
                .protocol(supportedProtocol);
    }

    private Consumer<SslProvider.SslContextSpec> getSslContextSpecConsumer() {
        return sslContextSpec -> {
            if (this.sslContext == null) {
                log.error("sslContextSupplier shouldn't supply null please provide sslContext");
            }
            sslContextSpec.sslContext(this.sslContext).setSniAsyncMappings(this.asyncMapping);
        };
    }
}
