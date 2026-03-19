package com.asrevo.cvhome.uaa.sdk;

import com.asrevo.cvhome.uaa.sdk.dto.PageResponse;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class AbstractAdminClient {

	protected final OAuth2TokenManager tokenManager;

	protected final HttpClient httpClient;

	protected final ObjectMapper objectMapper;

	protected AbstractAdminClient(String baseUrl, String clientId, String clientSecret) {
		this.tokenManager = new OAuth2TokenManager(baseUrl, clientId, clientSecret);
		this.httpClient = HttpClient.newBuilder().build();
		this.objectMapper = new ObjectMapper()/* .registerModule(new JavaTimeModule()) */;
	}

	protected HttpRequest.Builder authenticatedRequestBuilder(String url) {
		return HttpRequest.newBuilder()
			.uri(URI.create(url))
			.header("Authorization", "Bearer " + tokenManager.getAccessToken());
	}

	protected <T> T sendAndParse(HttpRequest request, Class<T> responseType) {
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			verifyResponse(response);
			return objectMapper.readValue(response.body(), responseType);
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	protected <T> T sendAndParse(HttpRequest request, TypeReference<T> responseType) {
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			verifyResponse(response);
			return objectMapper.readValue(response.body(), responseType);
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	protected <T> PageResponse<T> sendAndParsePage(HttpRequest request, TypeReference<PageResponse<T>> typeReference) {
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			verifyResponse(response);
			return objectMapper.readValue(response.body(), typeReference);
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	protected void sendAndVerify(HttpRequest request) {
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			verifyResponse(response);
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	protected void verifyResponse(HttpResponse<String> response) {
		if (response.statusCode() >= 400) {
			throw new RuntimeException("API call failed with status " + response.statusCode() + ": " + response.body());
		}
	}

}
