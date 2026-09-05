package com.asrevo.cvhome.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.checkout.api.errors.CheckoutApiErrors;
import com.asrevo.cvhome.checkout.services.order.ExternalOrderSignalService;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    /**
     * The only outbound call: telling checkout that a reservation expired before it was committed.
     */
    @Bean
    public ExternalOrderSignalService externalOrderSignalService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient("checkout", ExternalOrderSignalService.class, CheckoutApiErrors.CATALOG);
    }

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
