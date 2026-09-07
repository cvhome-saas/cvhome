package com.asrevo.cvhome.gateway.impersonation;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * What the impersonation service needs besides the shared client machinery: a clock, and a plain client for uaa.
 *
 * <p>
 * Deliberately no {@code ReactiveOAuth2AuthorizedClientManager} of its own. The gateway holds exactly one — the
 * service-backed manager the shared configuration registers — and Spring Cloud Gateway's {@code tokenRelay()}
 * resolves it with {@code getIfAvailable}, which refuses to choose between two. The impersonation service uses
 * that one manager to refresh the operator's token, and writes the swapped client into the
 * {@code ReactiveOAuth2AuthorizedClientService} the manager reads.
 * </p>
 */
@Configuration
public class ImpersonationConfig {

    /** The clock the expiry ceiling is judged by; a test replaces it. */
    @Bean
    @ConditionalOnMissingBean(Clock.class)
    Clock impersonationClock() {
        return Clock.systemUTC();
    }

    /** Talks to uaa's token and revocation endpoints at uaa's own address, authenticating as the client itself. */
    @Bean("impersonationUaaClient")
    WebClient impersonationUaaClient() {
        return WebClient.builder().build();
    }

}
