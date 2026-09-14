package com.asrevo.cvhome.s2s.config.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.s2s.model.S2sHttpProperties;
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A peer that does not answer in time fails the call instead of holding the caller.
 *
 * <p>
 * Nothing set a timeout before: in the 2026-09-14 spike checkout waited on a slow catalog for as long as catalog took,
 * holding its database connections the whole time, and every other checkout request failed behind it.
 * </p>
 */
class S2sRequestFactoriesTest {

    private static final Duration SLOW_ANSWER = Duration.ofMillis(1500);

    private static final String LATE = "late";

    private static final String SLOW_PATH = "/slow";

    private static final String CATALOG = "catalog";

    private static final String PAYMENT = "payment";

    private HttpServer server;

    private ExecutorService executor;

    @BeforeEach
    void startASlowPeer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(SLOW_PATH, exchange -> {
            try {
                Thread.sleep(SLOW_ANSWER);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = LATE.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        server.start();
    }

    @AfterEach
    void stopThePeer() {
        server.stop(0);
        executor.shutdownNow();
    }

    @Test
    void aPeerThatDoesNotAnswerWithinTheReadTimeoutFailsTheCall() {
        S2sRequestFactories factories = new S2sRequestFactories(
                new S2sHttpProperties(Duration.ofSeconds(1), Duration.ofMillis(200), Map.of()));

        long started = System.nanoTime();
        assertThatThrownBy(() -> get(factories, CATALOG)).isInstanceOf(ResourceAccessException.class);
        assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(SLOW_ANSWER);
    }

    @Test
    void aPeerNamedInReadTimeoutsGetsItsLongerWait() {
        S2sRequestFactories factories = new S2sRequestFactories(new S2sHttpProperties(Duration.ofSeconds(1),
                Duration.ofMillis(200), Map.of(PAYMENT, Duration.ofSeconds(5))));

        assertThat(get(factories, PAYMENT)).isEqualTo(LATE);
    }

    @Test
    void unsetTimeoutsFallBackToOneSecondToConnectAndThreeToAnswer() {
        S2sHttpProperties defaults = new S2sHttpProperties(null, null, null);

        assertThat(defaults.connectTimeout()).isEqualTo(Duration.ofSeconds(1));
        assertThat(defaults.readTimeoutFor(CATALOG)).isEqualTo(Duration.ofSeconds(3));
    }

    private String get(S2sRequestFactories factories, String service) {
        return RestClient.builder()
                .requestFactory(factories.forService(service))
                .baseUrl(String.format("http://127.0.0.1:%d", server.getAddress().getPort()))
                .build()
                .get().uri(SLOW_PATH).retrieve().body(String.class);
    }
}
