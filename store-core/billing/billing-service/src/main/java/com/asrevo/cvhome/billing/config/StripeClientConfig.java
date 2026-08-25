package com.asrevo.cvhome.billing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.stripe.StripeClient;

/**
 * The one {@link StripeClient} every gateway calls through.
 *
 * <p>
 * Built with the resolved credential so a client constructed here is usable on its own, but the credential is still
 * passed per call in {@code RequestOptions} — the SDK merges the two and the per-call authenticator wins, so the key
 * a call is made with stays visible at its call site rather than becoming ambient.
 * </p>
 *
 * <p>
 * A null key is not fatal, exactly as it is not for {@link StripeCredentials}: the catalog, existing subscriptions
 * and every entitlement read work without Stripe, and a call made with no credential fails as an authentication
 * error at the moment it is attempted rather than preventing the service from starting.
 * </p>
 */
@Configuration(proxyBeanMethods = false)
public class StripeClientConfig {

    @Bean
    StripeClient stripeClient(StripeCredentials credentials) {
        return new StripeClient(credentials.apiKey());
    }

}
