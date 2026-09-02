package com.asrevo.cvhome.uaa.ratelimit;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Per-address limits on the endpoints that take a secret: the sign-in form, the token endpoint and the public
 * one-time-link endpoints. A fixed window per address and rule.
 */
@ConfigurationProperties("com.asrevo.cvhome.uaa.rate-limit")
public record RateLimitProperties(boolean enabled, Rule login, Rule token, Rule publicApi) {

    public RateLimitProperties {
        login = login == null ? new Rule(10, Duration.ofMinutes(1)) : login;
        token = token == null ? new Rule(60, Duration.ofMinutes(1)) : token;
        publicApi = publicApi == null ? new Rule(20, Duration.ofMinutes(1)) : publicApi;
    }

    /** @param limit attempts allowed per {@code window} from one address */
    public record Rule(int limit, Duration window) {
    }

}
