package com.asrevo.cvhome.sso.idp.egress;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.asrevo.cvhome.sso.ratelimit.RateLimitProperties;
import com.asrevo.cvhome.sso.ratelimit.RateLimiter;
import com.asrevo.cvhome.sso.realm.SsoTenantIdentifierResolver;
import com.asrevo.cvhome.uaa.errors.IdpEndpointRefusedException;
import com.asrevo.cvhome.uaa.errors.IdpTestThrottledException;

import lombok.extern.slf4j.Slf4j;

/**
 * Decides whether the server may fetch a merchant-supplied URL.
 *
 * <p>
 * Checked twice on purpose: once when a provider is saved, so a hostile endpoint is never stored and never
 * reached by the sign-in flow that would use it later, and again immediately before the {@code test} action
 * fetches one. The second check is what a name that resolves publicly at save time and privately a minute later
 * has to get past.
 * </p>
 *
 * <p>
 * <strong>It is not complete, and the gap is worth naming.</strong> Between this check and the socket being
 * opened, the name is resolved a second time by the HTTP client, and nothing here makes those two answers the
 * same. Closing that needs the connection pinned to the address that was checked, which means owning the
 * connection factory. What this does close is every static case — a literal private address, a metadata IP, a
 * plain-HTTP endpoint, a name that only ever resolves inside — and it narrows the rest to an attacker who
 * controls a DNS server and wins a race.
 * </p>
 */
@Slf4j
@Component
public class EgressGuard {

    /** The cloud metadata service. Reachable from anything in the VPC, and it answers with credentials. */
    private static final String METADATA = "169.254.169.254";

    private static final int CGNAT_FIRST = 100;

    private static final int CGNAT_LOW = 64;

    private static final int CGNAT_HIGH = 127;

    private final EgressPolicy policy;

    private final SsoTenantIdentifierResolver realms;

    private final RateLimiter tests;

    public EgressGuard(EgressPolicy policy, SsoTenantIdentifierResolver realms) {
        this.policy = policy;
        this.realms = realms;
        this.tests = new RateLimiter(
                new RateLimitProperties.Rule(policy.testsPerRealmPerHour(), Duration.ofHours(1)));
    }

    /**
     * Spends one of the realm's on-demand fetches.
     *
     * <p>
     * Per realm rather than per address: the caller is a signed-in merchant, and what is being rationed is this
     * server's willingness to make a request somebody else chose. Unlimited, the test button reports whether a
     * host answered — which, aimed at an internal address range, is a port scan with a progress bar.
     * </p>
     */
    public void takeTestBudget(String alias) throws IdpTestThrottledException {
        if (!tests.tryAcquire(realms.resolveCurrentTenantIdentifier())) {
            throw IdpTestThrottledException.of(alias);
        }
    }

    /**
     * @param field the request field being checked, so a refusal names it
     * @throws IdpEndpointRefusedException when the URL is malformed, not an allowed scheme, or resolves to an
     *                                     address the server must not reach on somebody else's instruction
     */
    public void check(String field, String url) throws IdpEndpointRefusedException {
        if (!StringUtils.hasText(url)) {
            return;
        }
        URI uri = parse(field, url);
        if (!policy.schemes().contains(scheme(uri))) {
            throw refuse(field, url, "scheme %s".formatted(scheme(uri)));
        }
        if (uri.getUserInfo() != null) {
            // Credentials in the URL are never needed here and are a way to smuggle a different host past a
            // reader: https://provider.example@10.0.0.5/ is a request to 10.0.0.5.
            throw refuse(field, url, "credentials in the URL");
        }
        String host = uri.getHost();
        if (!StringUtils.hasText(host)) {
            throw refuse(field, url, "no host");
        }
        if (policy.allowPrivateAddresses()) {
            return;
        }
        for (InetAddress address : resolve(field, url, host)) {
            if (isInternal(address)) {
                throw refuse(field, url, "resolves to %s".formatted(address.getHostAddress()));
            }
        }
    }

    private static URI parse(String field, String url) throws IdpEndpointRefusedException {
        try {
            URI uri = URI.create(url.trim());
            if (!uri.isAbsolute()) {
                throw IdpEndpointRefusedException.of(field);
            }
            return uri;
        } catch (IllegalArgumentException malformed) {
            throw IdpEndpointRefusedException.of(field);
        }
    }

    private static String scheme(URI uri) {
        return uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
    }

    private InetAddress[] resolve(String field, String url, String host) throws IdpEndpointRefusedException {
        try {
            return InetAddress.getAllByName(host);
        } catch (UnknownHostException unresolvable) {
            // Refused rather than allowed: an endpoint whose address cannot be checked has not been checked.
            throw refuse(field, url, "does not resolve");
        }
    }

    /**
     * Every address family's way of saying "inside". Java answers four of these; unique-local IPv6 and the
     * carrier-grade NAT range it does not, so they are spelled out.
     */
    private static boolean isInternal(InetAddress address) {
        return knownToJava(address) || METADATA.equals(address.getHostAddress()) || unknownToJava(address);
    }

    private static boolean knownToJava(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
            return true;
        }
        return address.isLinkLocalAddress() || address.isSiteLocalAddress() || address.isMulticastAddress();
    }

    /** The ranges Java has no predicate for: {@code 0.0.0.0/8}, {@code 100.64.0.0/10} and {@code fc00::/7}. */
    private static boolean unknownToJava(InetAddress address) {
        byte[] octets = address.getAddress();
        if (octets.length != 4) {
            return (octets[0] & 0xfe) == 0xfc;
        }
        int first = octets[0] & 0xff;
        int second = octets[1] & 0xff;
        return first == 0 || first == CGNAT_FIRST && second >= CGNAT_LOW && second <= CGNAT_HIGH;
    }

    private static IdpEndpointRefusedException refuse(String field, String url, String why) {
        log.warn("Refusing identity-provider endpoint {} ({}): {}", field, url, why);
        return IdpEndpointRefusedException.of(field);
    }

}
