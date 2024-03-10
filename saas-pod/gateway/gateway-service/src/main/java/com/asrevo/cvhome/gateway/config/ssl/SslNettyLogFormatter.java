package com.asrevo.cvhome.gateway.config.ssl;

import reactor.netty.http.server.ConnectionInformation;
import reactor.netty.http.server.logging.AccessLog;
import reactor.netty.http.server.logging.AccessLogArgProvider;
import reactor.netty.http.server.logging.AccessLogFactory;
import reactor.util.annotation.Nullable;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.ZonedDateTime;
import java.util.Optional;

class SslNettyLogFormatter implements AccessLogFactory {
    public static final String MISSING = "-";
    private static final String DEFAULT_LOG_FORMAT = "{} - {} [{}] \"{} {} {}\" {} {} {} {}";

    static String applyAddress(@Nullable SocketAddress socketAddress) {
        return socketAddress instanceof InetSocketAddress ? ((InetSocketAddress) socketAddress).getHostString() : MISSING;
    }

    @Override
    public AccessLog apply(AccessLogArgProvider args) {
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
    }

}
