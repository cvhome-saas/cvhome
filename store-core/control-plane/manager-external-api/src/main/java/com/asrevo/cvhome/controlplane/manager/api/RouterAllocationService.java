package com.asrevo.cvhome.controlplane.manager.api;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/api/v1/router")
public interface RouterAllocationService {

	@GetExchange("store-pod-by-store-id")
	Mono<Pod> getStorePodByStoreId(@RequestParam("store") ManagerStoreId store);

}
