package com.asrevo.cvhome.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    /**
     * The only outbound call: telling checkout that a reservation expired before it was committed.
     */
    @Bean
    public ExternalOrderService externalOrderService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient("checkout", ExternalOrderService.class, RemoteErrorCatalog.none());
    }
}
