package com.asrevo.cvhome.manager.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

public record ProxyClient(WebClient storeProxyClient) {

	public <T> Mono<ResponseEntity<T>> forward(HttpMethod method, String uri,
			Supplier<BodyInserter<?, ? super ClientHttpRequest>> bodyExtractor,
			Function<ClientResponse, Mono<ResponseEntity<T>>> bodyyy) {
		WebClient.RequestBodySpec requestSpec = storeProxyClient.method(method).uri(uri);
		return requestSpec.body(bodyExtractor.get()).exchangeToMono(bodyyy);
	}

}
