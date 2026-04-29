package com.asrevo.cvhome.commons.http;

import java.net.http.HttpClient;

/**
 * Interface to customize the {@link HttpClient} used by the SDK.
 */
@FunctionalInterface
public interface HttpClientCustomizer {
    /**
     * Customizes the given {@link HttpClient.Builder} and returns the built {@link HttpClient}.
     * Alternatively, it can just return a pre-configured {@link HttpClient}.
     * 
     * @param builder the builder to customize
     * @return the customized HttpClient
     */
    HttpClient customize(HttpClient.Builder builder);
}
