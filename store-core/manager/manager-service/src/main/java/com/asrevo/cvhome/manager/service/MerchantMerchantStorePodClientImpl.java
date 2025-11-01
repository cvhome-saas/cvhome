package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class MerchantMerchantStorePodClientImpl implements MerchantStorePodClient {

	private static final String MERCHANT_BASE_URL = "/merchant/api/v1";

	@Getter
	private final ProxyClient proxyClient;

	@Override
	public Mono<ResponseEntity<Void>> create(Map<Object, Object> dto) {
		Function<ClientResponse, Mono<ResponseEntity<Void>>> responseType = it -> it.toEntity(Void.class);
		ProxyClient.SendRequest<Void> request = ProxyClient.SendRequest.builder(HttpMethod.POST, responseType)
			.uri(MERCHANT_BASE_URL + "/private/store")
			.headers(Map.of())
			.body(Mono.just(dto), new ParameterizedTypeReference<>() {
			})
			.build();
		return proxyClient.send(request);
	}

	@Override
	public Mono<ResponseEntity<Map<String, Object>>> getStore(String store) {
		Function<ClientResponse, Mono<ResponseEntity<Map<String, Object>>>> responseType = it -> it
			.toEntity(new ParameterizedTypeReference<>() {
			});

		ProxyClient.SendRequest<Map<String, Object>> request = ProxyClient.SendRequest
			.builder(HttpMethod.GET, responseType)
			.uri(MERCHANT_BASE_URL + "/private/store?store=" + store)
			.headers(Map.of())
			.build();

		return proxyClient.send(request);
	}

}
