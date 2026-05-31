package com.asrevo.cvhome.checkout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.catalog.services.product.ExternalPaymentGatewayService;
import com.asrevo.cvhome.catalog.services.product.ExternalProductReservationService;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.catalog.services.product.model.PaymentResponse;
import com.asrevo.cvhome.catalog.services.product.model.PaymentStatus;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

@Configuration
public class ClientsConfig {

    private static final String CATALOG_SERVICE_NAME = "catalog";

    private static final String MERCHANT_SERVICE_NAME = "merchant";

    @Bean
    public ExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
        ExternalMerchantStoreService externalMerchantStoreService = restClientBuilder.buildClient(MERCHANT_SERVICE_NAME,
                ExternalMerchantStoreService.class);
        return new CachedExternalMerchantStoreService(externalMerchantStoreService);
    }

    @Bean
    public ExternalProductService externalProductService(RestClientBuilder restClientBuilder) {
        ExternalProductService externalProductService = restClientBuilder.buildClient(CATALOG_SERVICE_NAME,
                ExternalProductService.class);
        return new CachedExternalProductService(externalProductService);
    }

    @Bean
    public ExternalProductReservationService externalProductReservationService(RestClientBuilder restClientBuilder) {
        return restClientBuilder.buildClient(CATALOG_SERVICE_NAME, ExternalProductReservationService.class);
    }

    @Bean
    public ExternalPaymentGatewayService externalPaymentGatewayService() {
        return r -> {
            return new PaymentResponse(PaymentStatus.REDIRECT_REQUIRED,
                    "https://google.com/" + r.orderId() + "?currency=" + r.currency().code() + "&amount=" + r.amount());
        };
    }

}
