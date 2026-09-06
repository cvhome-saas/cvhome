package com.asrevo.cvhome.checkout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiErrors;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;
import com.asrevo.cvhome.inventory.services.ExternalProductReservationService;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.payment.api.errors.PaymentApiErrors;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

/**
 * The four neighbours checkout talks to. Each proxy is built with that API's error catalog, so a refusal and an outage
 * arrive as different exceptions — the distinction placement is built on.
 */
@Configuration
public class ClientsConfig {

    private static final String MERCHANT = "merchant";

    private static final String CATALOG = "catalog";

    private static final String INVENTORY = "inventory";

    private static final String PAYMENT = "payment";

    /** Also what the authorization layer uses to learn which org owns a store. */
    @Bean
    public ExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
        return new CachedExternalMerchantStoreService(restClientBuilder.buildClient(MERCHANT,
                ExternalMerchantStoreService.class, RemoteErrorCatalog.none()));
    }

    @Bean
    public ExternalProductService externalProductService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(CATALOG, ExternalProductService.class, RemoteErrorCatalog.none());
    }

    /** Deliberately uncached: a price or a stock figure is only right if it is live. */
    @Bean
    public ExternalInventoryService externalInventoryService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(INVENTORY, ExternalInventoryService.class, RemoteErrorCatalog.none());
    }

    @Bean
    public ExternalProductReservationService externalProductReservationService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(INVENTORY, ExternalProductReservationService.class,
                InventoryApiErrors.INVENTORY);
    }

    @Bean
    public ExternalPaymentGatewayService externalPaymentGatewayService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(PAYMENT, ExternalPaymentGatewayService.class, PaymentApiErrors.CATALOG);
    }
}
