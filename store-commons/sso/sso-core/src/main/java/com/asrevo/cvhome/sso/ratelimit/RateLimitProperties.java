package com.asrevo.cvhome.sso.ratelimit;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Limits on the endpoints that take a secret: the sign-in form, the token endpoint and the public one-time-link
 * endpoints. A fixed window, counted twice.
 *
 * <p>
 * Each rule's limit is what one address may spend <em>on one realm</em>. One deployment serves every store on a
 * pod, so a single address counted once could spend the whole budget against store A and leave store B's
 * shoppers refused for the rest of the window — a store's traffic is not another store's to consume. The address
 * is still counted on its own, at {@code spread} times the limit, so an attacker who spreads the same burst
 * across a thousand stores is caught by the second counter rather than let through by the first.
 * </p>
 *
 * <p>
 * A single-realm deployment counts the same address twice with the looser bound never binding first, so uaa
 * behaves exactly as it did.
 * </p>
 *
 * @param spread how many realms one address may spend a full budget across before the address itself is refused
 */
@ConfigurationProperties("com.asrevo.cvhome.uaa.rate-limit")
public record RateLimitProperties(boolean enabled, Rule login, Rule token, Rule publicApi, int spread) {

    private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(1);

    private static final int DEFAULT_SPREAD = 5;

    public RateLimitProperties {
        spread = spread > 0 ? spread : DEFAULT_SPREAD;
        login = login == null ? new Rule(10, DEFAULT_WINDOW) : login;
        token = token == null ? new Rule(60, DEFAULT_WINDOW) : token;
        publicApi = publicApi == null ? new Rule(20, DEFAULT_WINDOW) : publicApi;
    }

    /** @param limit attempts allowed per {@code window} from one address */
    public record Rule(int limit, Duration window) {

        /*
         * A rule that names only its limit is the ordinary way to tighten or relax one, and binding left the
         * window null — which surfaced as a NullPointerException while the web server was starting, from a
         * property that read as perfectly reasonable. The window defaults on its own now.
         */
        public Rule {
            window = window == null ? DEFAULT_WINDOW : window;
        }
    }

}
