package com.asrevo.cvhome.s2s.model;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * How long a service waits on a peer it calls over {@code RestClientBuilder}.
 *
 * <p>
 * There were no timeouts at all: in the 2026-09-14 spike checkout held its three database connections while catalog
 * took seconds to answer, and everything behind them waited 30 s for a connection and failed. A call that cannot
 * connect in a second or answer in a few is better failed, so the caller's own request fails fast and frees what it
 * holds.
 * </p>
 *
 * @param connectTimeout how long to wait for a connection to the peer
 * @param readTimeout    how long to wait for the peer's answer, unless {@code readTimeouts} names the peer
 * @param readTimeouts   a longer wait for the peers whose answer legitimately takes longer, by service name (payment
 *                       waits on its provider)
 */
@ConfigurationProperties("com.asrevo.cvhome.s2s.http")
public record S2sHttpProperties(Duration connectTimeout, Duration readTimeout, Map<String, Duration> readTimeouts) {

    public S2sHttpProperties {
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(1) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(3) : readTimeout;
        readTimeouts = readTimeouts == null ? Map.of() : Map.copyOf(readTimeouts);
    }

    public Duration readTimeoutFor(String serviceName) {
        return readTimeouts.getOrDefault(serviceName, readTimeout);
    }
}
