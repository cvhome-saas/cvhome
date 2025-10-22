package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.manager.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class PodProxyServiceImpl implements PodProxyService {

	private static final String CONTROLLER_BASE_URL = "/api/v1/pod-proxy";

	private final StorePodClientFactory factory;

	private final InternalStoreService internalStoreService;

	@Override
	public Mono<ResponseEntity<byte[]>> forward(ManagerStoreId store, ServerHttpRequest request) {
		ProxyClient proxyClient = getProxyClient(store);
		return execute(proxyClient, request);
	}

	@Override
	public Mono<ResponseEntity<byte[]>> forward(PodId podId, ServerHttpRequest request) {
		ProxyClient proxyClient = getProxyClient(podId);
		return execute(proxyClient, request);
	}

	private Mono<ResponseEntity<byte[]>> execute(ProxyClient proxyClient, ServerHttpRequest request) {
		BodyInserter<Flux<DataBuffer>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters
			.fromPublisher(request.getBody(), DataBuffer.class);

		return proxyClient.forward(request.getMethod(), buildUri(request).toUriString(),
				request.getHeaders().asSingleValueMap(), () -> bodyInserter, it -> it.toEntity(byte[].class));
	}

	private ProxyClient getProxyClient(ManagerStoreId store) {
		Pod pod = internalStoreService.getStorePod(store);
		return getProxyClient(pod.id());
	}

	private ProxyClient getProxyClient(PodId podId) {
		StorePodClientImpl client = ((StorePodClientImpl) factory.getClient(podId));
		return client.getProxyClient();
	}

	private UriComponents buildUri(ServerHttpRequest request) {
		return UriComponentsBuilder.fromPath(request.getURI().getPath().replaceFirst(CONTROLLER_BASE_URL, ""))
			.replaceQueryParams(request.getQueryParams())
			.build(true);
	}

}
