package com.asrevo.cvhome.manager.api;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/api/v1/router")
public interface RouterAllocationService {

	@GetExchange("store-id-by-domain")
	Mono<ManagerStoreId> getStorePodByStoreId(Domain domain);

	@GetExchange("store-pod-by-store-id")
	Mono<Pod> getStorePodByStoreId(@RequestParam("store") ManagerStoreId store);

}
