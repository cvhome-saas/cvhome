package com.asrevo.cvhome.sso.idp.egress;

import java.time.Duration;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * What the server is allowed to fetch on a merchant's instruction.
 *
 * <p>
 * Identity-provider endpoints are merchant-entered URLs that this server then requests — on save, on the
 * {@code test} action, and on every sign-in through that provider. Unbounded, that is a request forger sitting
 * inside the network: cloud metadata at {@code 169.254.169.254}, another service on the same subnet, a database
 * admin port. The defaults are the safe ones, so a deployment that configures nothing is protected.
 * </p>
 *
 * <p>
 * {@code allowPrivateAddresses} exists for the local stack and for integration tests, which point providers at a
 * stub on {@code localhost}. It is off by default and turning it on is a deployment's explicit decision — the
 * one place this control can be lost, said out loud rather than buried in a default.
 * </p>
 *
 * @param schemes              URL schemes a provider endpoint may use
 * @param allowPrivateAddresses whether an endpoint may resolve inside the server's own network
 * @param timeout              how long a fetch may take before it is abandoned
 * @param maxResponseBytes     how much of a response is read before it is abandoned
 * @param testsPerRealmPerHour how many times one realm may use the {@code test} action in an hour
 */
@ConfigurationProperties("com.asrevo.cvhome.sso.egress")
public record EgressPolicy(Set<String> schemes, boolean allowPrivateAddresses, Duration timeout,
                           int maxResponseBytes, int testsPerRealmPerHour) {

    private static final int DEFAULT_MAX_RESPONSE = 256 * 1024;

    public EgressPolicy {
        schemes = schemes == null || schemes.isEmpty() ? Set.of("https") : Set.copyOf(schemes);
        timeout = timeout == null ? Duration.ofSeconds(5) : timeout;
        maxResponseBytes = maxResponseBytes > 0 ? maxResponseBytes : DEFAULT_MAX_RESPONSE;
        testsPerRealmPerHour = testsPerRealmPerHour > 0 ? testsPerRealmPerHour : 30;
    }

}
