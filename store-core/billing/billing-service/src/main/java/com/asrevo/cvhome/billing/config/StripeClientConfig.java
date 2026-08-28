package com.asrevo.cvhome.billing.config;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.stripe.StripeClient;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.StripeException;
import com.stripe.model.StripeObject;
import com.stripe.net.ApiMode;
import com.stripe.net.ApiResource;
import com.stripe.net.BaseAddress;
import com.stripe.net.RequestOptions;
import com.stripe.net.StripeResponseGetter;

/**
 * The one {@link StripeClient} every gateway calls through.
 *
 * <p>
 * Built with the resolved credential so a client constructed here is usable on its own, but the credential is still
 * passed per call in {@code RequestOptions} — the SDK merges the two and the per-call authenticator wins, so the key
 * a call is made with stays visible at its call site rather than becoming ambient.
 * </p>
 */
@Configuration(proxyBeanMethods = false)
public class StripeClientConfig {

    private static final String NO_KEY =
            "No API key provided. Set com.asrevo.cvhome.stripe.key to make calls to Stripe.";

    /**
     * @return a client bound to the configured key, or one that refuses every call when there is none
     */
    @Bean
    StripeClient stripeClient(StripeCredentials credentials) {
        // A missing credential is not fatal, exactly as it is not for StripeCredentials: the catalog, existing
        // subscriptions and every entitlement read work without Stripe, and refusing to start would make a service
        // that is mostly not about Stripe unbootable in an environment that has no credentials yet.
        //
        // StripeClient(String) rejects a null key outright, so an unconfigured deployment gets a client that fails
        // each call with AuthenticationException instead — which is what the SDK's static resource methods did
        // before this seam existed, and which reaches no network.
        return credentials.configured()
                ? new StripeClient(credentials.apiKey())
                : new StripeClient(new UnconfiguredResponseGetter());
    }

    /**
     * Answers every request with the SDK's own "no API key" failure, without leaving the JVM.
     */
    private static final class UnconfiguredResponseGetter implements StripeResponseGetter {

        @Override
        public <T extends StripeObject> T request(BaseAddress baseAddress, ApiResource.RequestMethod method,
                                                  String path, Map<String, Object> params, Type typeToken,
                                                  RequestOptions options, ApiMode apiMode) throws StripeException {
            throw noKey();
        }

        @Override
        public InputStream requestStream(BaseAddress baseAddress, ApiResource.RequestMethod method, String path,
                                         Map<String, Object> params, RequestOptions options, ApiMode apiMode)
                throws StripeException {
            throw noKey();
        }

        private static AuthenticationException noKey() {
            return new AuthenticationException(NO_KEY, null, null, 0);
        }

    }

}
