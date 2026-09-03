package com.asrevo.cvhome.sso.ratelimit;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Per-address limits on the endpoints that take a secret: the sign-in form, the token endpoint and the public
 * one-time-link endpoints. A fixed window per address and rule.
 */
@ConfigurationProperties("com.asrevo.cvhome.uaa.rate-limit")
public record RateLimitProperties(boolean enabled, Rule login, Rule token, Rule publicApi) {

    private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(1);

    public RateLimitProperties {
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
