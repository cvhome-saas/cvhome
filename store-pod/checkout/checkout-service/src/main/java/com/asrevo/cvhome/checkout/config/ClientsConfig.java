package com.asrevo.cvhome.checkout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.catalog.services.product.ExternalProductReservationService;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.payment.api.errors.PaymentApiErrors;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    private static final String CATALOG_SERVICE_NAME = "catalog";

    private static final String MERCHANT_SERVICE_NAME = "merchant";

    private static final String PAYMENT_SERVICE_NAME = "payment";

    @Bean
    public ExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
        ExternalMerchantStoreService externalMerchantStoreService = restClientBuilder.buildClient(MERCHANT_SERVICE_NAME,
                ExternalMerchantStoreService.class, RemoteErrorCatalog.none());
        return new CachedExternalMerchantStoreService(externalMerchantStoreService);
    }

    @Bean
    public ExternalProductService externalProductService(RestClientBuilder restClientBuilder) {
        ExternalProductService externalProductService = restClientBuilder.buildClient(CATALOG_SERVICE_NAME,
                ExternalProductService.class, RemoteErrorCatalog.none());
        return new CachedExternalProductService(externalProductService);
    }

    @Bean
    public ExternalProductReservationService externalProductReservationService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(CATALOG_SERVICE_NAME, ExternalProductReservationService.class, RemoteErrorCatalog.none());
    }

    /**
     * Exposes the wrapper, not the raw proxy: callers depend on the payment API's caller-side vocabulary, and the
     * proxy's own signature carries the payment service's instead.
     *
     * <p>
     * The only client here with an error contract, so a payment failure arrives as a type checkout can branch on
     * rather than as an opaque remote error. The other three APIs name none of their failures yet; passing
     * {@code null} is how that is stated.
     * </p>
     */
    @Bean
    public ExternalPaymentGatewayService externalPaymentGatewayService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(PAYMENT_SERVICE_NAME,
                ExternalPaymentGatewayService.class, PaymentApiErrors.CATALOG);
    }

}
