package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public interface PodProxyService {

	Mono<ResponseEntity<byte[]>> forward(ManagerStoreId store, ServerHttpRequest request);

	Mono<ResponseEntity<byte[]>> forward(PodId podId, ServerHttpRequest request);

}
