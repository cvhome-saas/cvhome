package com.asrevo.cvhome.gateway.impersonation;

import java.time.Clock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

import com.asrevo.cvhome.s2s.config.internal.ServiceUrlBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

/**
 * The two HTTP clients the impersonation service needs.
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

    /** Where {@code lb://tenancy} is from here — the same resolution every declarative client uses. */
    @Bean("impersonationUrls")
    ServiceUrlBuilder impersonationUrls(ServiceDomainProperties services, Environment environment) {
        return new ServiceUrlBuilder(services, environment);
    }

    /** Talks to uaa's token and revocation endpoints at uaa's own address, authenticating as the client itself. */
    @Bean("impersonationUaaClient")
    WebClient impersonationUaaClient() {
        return WebClient.builder().build();
    }

    /**
     * Talks to tenancy through {@code lb://} with a bearer of our choosing. Not the shared
     * {@code microServiceWebClientBuilder}: its filter attaches the gateway's own {@code s2s} token to every call,
     * and this one must carry the <em>impersonated</em> token — the point of the call is what that token may see.
     */
    @Bean("impersonationTenancyClient")
    WebClient impersonationTenancyClient(LoadBalancedExchangeFilterFunction loadBalancer) {
        return WebClient.builder().filter(loadBalancer).build();
    }

}
