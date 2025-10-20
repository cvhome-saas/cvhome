package com.asrevo.cvhome.merchant.api;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/api/v1")
public interface ExternalReactiveMerchantStoreService {

	@GetExchange("store")
	Mono<ReadableMerchantStore> getStore(StoreMerchantId store);

}
