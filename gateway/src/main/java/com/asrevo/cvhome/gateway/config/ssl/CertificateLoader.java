package com.asrevo.cvhome.gateway.config.ssl;

import reactor.netty.tcp.SslProvider;


public interface CertificateLoader {
    SslProvider load(String domain);
    SslProvider defaultSslProvider();

}
