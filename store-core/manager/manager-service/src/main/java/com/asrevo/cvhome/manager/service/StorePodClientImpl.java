package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.merchant.api.StorePodClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class StorePodClientImpl implements StorePodClient {

	@Getter
	private final ProxyClient proxyClient;

	@Override
	public Mono<ResponseEntity<Void>> create(Map<Object, Object> dto) {
		Supplier<BodyInserter<?, ? super ClientHttpRequest>> bodyExtractor = () -> BodyInserters.fromValue(dto);
		Function<ClientResponse, Mono<ResponseEntity<Void>>> responseType = it -> it.toEntity(Void.class);
		return proxyClient.forward(HttpMethod.POST, "/merchant/api/v1/private/store", Map.of(), bodyExtractor,
				responseType);
	}

	@Override
	public Mono<ResponseEntity<Map<String, Object>>> getStore(String store) {
		ParameterizedTypeReference<Map<String, Object>> typeReference = new ParameterizedTypeReference<>() {
		};
		Supplier<BodyInserter<?, ? super ClientHttpRequest>> bodyExtractor = () -> BodyInserters.fromValue(Map.of());
		Function<ClientResponse, Mono<ResponseEntity<Map<String, Object>>>> responseType = it -> it
			.toEntity(typeReference);
		return proxyClient.forward(HttpMethod.GET, "/merchant/api/v1/private/store?store=" + store, Map.of(),
				bodyExtractor, responseType);
	}

}
