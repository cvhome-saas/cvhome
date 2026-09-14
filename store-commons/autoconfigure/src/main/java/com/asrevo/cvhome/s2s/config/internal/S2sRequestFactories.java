package com.asrevo.cvhome.s2s.config.internal;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import com.asrevo.cvhome.s2s.model.S2sHttpProperties;

/**
 * The request factories every service-to-service {@code RestClient} runs on, one per read timeout.
 *
 * <p>
 * The JDK client is the one {@code RestClient.builder()} already picked on its own: servlet services keep webflux, and
 * with it Reactor Netty, off their runtime classpath, and nothing else is there. So this changes the timeouts and
 * nothing about how the calls are made. All factories share one {@link HttpClient}, and with it its connection pool.
 * </p>
 */
public final class S2sRequestFactories {

    private final HttpClient client;

    private final S2sHttpProperties properties;

    private final Map<Duration, ClientHttpRequestFactory> byReadTimeout = new ConcurrentHashMap<>();

    public S2sRequestFactories(S2sHttpProperties properties) {
        this.properties = properties;
        this.client = HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
    }

    /** The factory for calls to {@code serviceName}, with that peer's read timeout. */
    public ClientHttpRequestFactory forService(String serviceName) {
        return byReadTimeout.computeIfAbsent(properties.readTimeoutFor(serviceName), this::factory);
    }

    private ClientHttpRequestFactory factory(Duration readTimeout) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
