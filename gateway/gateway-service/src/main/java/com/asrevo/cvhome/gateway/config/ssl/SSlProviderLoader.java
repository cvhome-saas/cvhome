package com.asrevo.cvhome.gateway.config.ssl;

import reactor.netty.tcp.SslProvider;

public interface SSlProviderLoader {
    SslProvider load(String key);
}
