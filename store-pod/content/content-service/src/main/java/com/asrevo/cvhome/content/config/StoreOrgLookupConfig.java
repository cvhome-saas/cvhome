package com.asrevo.cvhome.content.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

/**
 * The one client this service has, and it is not for reading anything of its own.
 *
 * <p>
 * One, and not to read a store: it is what tells the authorization layer which organization owns the store a
 * request names. That is the question an org admin's token cannot answer on its own — the token says which
 * organization the person administers and nothing about who owns the store in the query parameter — so without
 * this client every org admin is refused. See {@code MerchantStoreOrgOwnerAutoConfiguration}, which turns having
 * the client into having the answer.
 * </p>
 */
@Configuration
public class StoreOrgLookupConfig {

    /**
     * Not for reading a store — this service never does. It is what tells the authorization layer which
     * organization owns the store a request names, which is the question an org admin's token cannot answer on
     * its own; without it every org admin is refused. See {@code MerchantStoreOrgOwnerAutoConfiguration}.
     */
    @Bean
    public ExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient("merchant", ExternalMerchantStoreService.class,
                RemoteErrorCatalog.none());
    }
}
