package com.asrevo.cvhome.order.config;

import com.asrevo.cvhome.catalog.services.product.ExternalProductReservationService;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientsConfig {

	@Bean
	public ExternalMerchantStoreService externalMerchantStoreService(RestClientBuilder restClientBuilder) {
		ExternalMerchantStoreService externalMerchantStoreService = restClientBuilder.buildClient("merchant",
				ExternalMerchantStoreService.class);
		return new CachedExternalMerchantStoreService(externalMerchantStoreService);
	}

	@Bean
	public ExternalProductService externalProductService(RestClientBuilder restClientBuilder) {
		ExternalProductService externalProductService = restClientBuilder.buildClient("catalog",
				ExternalProductService.class);
		return new CachedExternalProductService(externalProductService);
	}

	@Bean
	public ExternalProductReservationService externalProductReservationService(RestClientBuilder restClientBuilder) {
		return restClientBuilder.buildClient("catalog", ExternalProductReservationService.class);
	}

}
