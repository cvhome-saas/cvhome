package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.service.ProxyClient;
import com.asrevo.cvhome.manager.service.StorePodClientFactory;
import com.asrevo.cvhome.manager.service.StorePodClientImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/pod-proxy")
@AllArgsConstructor
@Slf4j
public class PodProxyController {

	private final StorePodClientFactory factory;

	private final InternalStoreService internalStoreService;

	private static final String CONTROLLER_BASE_URL = "/api/v1/pod-proxy";

	@RequestMapping("/**")
	public Mono<ResponseEntity<byte[]>> forward(ServerHttpRequest request, @RequestParam ManagerStoreId store) {
		ProxyClient proxyClient = getProxyClient(store);
		String uri = request.getURI().getPath().replaceFirst(CONTROLLER_BASE_URL, "") + "?store=" + store.getId();
		BodyInserter<Flux<DataBuffer>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters
			.fromPublisher(request.getBody(), DataBuffer.class);
		return proxyClient.forward(request.getMethod(), uri, request.getHeaders().asSingleValueMap(),
				() -> bodyInserter, it -> it.toEntity(byte[].class));
	}

	private ProxyClient getProxyClient(ManagerStoreId store) {
		Pod pod = internalStoreService.getStorePod(store);
		StorePodClientImpl client = ((StorePodClientImpl) factory.getClient(pod.id()));
		ProxyClient proxyClient = client.getProxyClient();
		return proxyClient;
	}

}
