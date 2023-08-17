package com.asrevo.cvhome.gateway.config.ssl;

import io.netty.handler.ssl.SslContext;
import io.netty.util.AsyncMapping;
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
    private final boolean redirectHttpToHttps = true;
    private final Supplier<SslContext> defaultSslContextSupplier;

    public DynamicSslLoaderNettyCustomizer(Supplier<SslContext> defaultSslContextSupplier, SSlProviderLoader slProviderLoader) {
        this.defaultSslContextSupplier = defaultSslContextSupplier;
        this.asyncMapping = (s, promise) -> {
            SslProvider load = slProviderLoader.load(s);
            if (load != null) {
                return promise.setSuccess(load);
            } else {
                return promise.setFailure(new Error("invalid host " + s));
            }
        };
    }

    static String applyAddress(@Nullable SocketAddress socketAddress) {
        return socketAddress instanceof InetSocketAddress ? ((InetSocketAddress) socketAddress).getHostString() :
                MISSING;
    }

    @Override
    public HttpServer apply(HttpServer httpServer) {
        return httpServer
                .accessLog(true, accessLogFactory)
                .secure(sslContextSpec -> {
                    SslContext sslContext = defaultSslContextSupplier.get();
                    if (sslContext == null) {
                        log.error("sslContextSupplier shouldn't supply null please provide sslContext");
                    }
                    sslContextSpec.sslContext(sslContext).setSniAsyncMappings(this.asyncMapping);
                }, redirectHttpToHttps).protocol(supportedProtocol);
    }
}
