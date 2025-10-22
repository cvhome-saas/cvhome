package com.asrevo.cvhome.manager.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public record ProxyClient(WebClient storeProxyClient) {

	public <T> Mono<ResponseEntity<T>> forward(HttpMethod method, String uri, Map<String, String> headers,
			Supplier<BodyInserter<?, ? super ClientHttpRequest>> bodyExtractor,
			Function<ClientResponse, Mono<ResponseEntity<T>>> bodyMapper) {

		WebClient.RequestBodySpec requestSpec = storeProxyClient.method(method).uri(uri);
		requestSpec.headers(httpHeaders -> headers.forEach(httpHeaders::add));
		return requestSpec.body(bodyExtractor.get()).exchangeToMono(bodyMapper);
	}

}
