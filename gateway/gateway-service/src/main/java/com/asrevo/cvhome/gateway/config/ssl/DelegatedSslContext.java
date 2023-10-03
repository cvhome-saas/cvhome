package com.asrevo.cvhome.gateway.config.ssl;


import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolNegotiator;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;
import java.util.List;

public class DelegatedSslContext extends SslContext {
    private SslContext cur;

    public DelegatedSslContext(SslContext cur) {
        this.cur = cur;
    }

    @Override
    public boolean isClient() {
        return cur.isClient();
    }

    @Override
    public List<String> cipherSuites() {
        return cur.cipherSuites();
    }

    @Override
    public ApplicationProtocolNegotiator applicationProtocolNegotiator() {
        return cur.applicationProtocolNegotiator();
    }

    @Override
    public SSLEngine newEngine(ByteBufAllocator alloc) {
        return cur.newEngine(alloc);
    }

    @Override
    public SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort) {
        return cur.newEngine(alloc, peerHost, peerPort);
    }

    @Override
    public SSLSessionContext sessionContext() {
        return cur.sessionContext();
    }

    void setNewSslContext(SslContext sslContext) {
        this.cur = sslContext;
    }
}
