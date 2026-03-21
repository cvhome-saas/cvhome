package com.asrevo.cvhome.uaa.sdk;

import com.asrevo.cvhome.uaa.sdk.dto.*;
import tools.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.Map;

public class AdminClientClient extends AbstractAdminClient {

	private final String clientsApiUrl;

	public AdminClientClient(String baseUrl, String clientId, String clientSecret) {
		super(baseUrl, clientId, clientSecret);
		this.clientsApiUrl = baseUrl + "/api/v1/admin/clients";
	}

	public PageResponse<ClientSummary> listClients(PageRequest pageRequest) {
		String url = clientsApiUrl;
		if (pageRequest != null) {
			url += "?page=" + pageRequest.page() + "&size=" + pageRequest.size();
		}
		HttpRequest request = authenticatedRequestBuilder(url).GET().build();
		return sendAndParsePage(request, new TypeReference<>() {
		});
	}

	public ClientDetails getClient(String id) {
		HttpRequest request = authenticatedRequestBuilder(clientsApiUrl + "/" + id).GET().build();
		return sendAndParse(request, ClientDetails.class);
	}

	public ClientDetails createClient(ClientDetails req) {
		HttpRequest request = authenticatedRequestBuilder(clientsApiUrl)
			.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
			.header("Content-Type", "application/json")
			.build();
		return sendAndParse(request, ClientDetails.class);
	}

	public ClientDetails updateClient(String id, ClientDetails req) {
		HttpRequest request = authenticatedRequestBuilder(clientsApiUrl + "/" + id)
			.PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
			.header("Content-Type", "application/json")
			.build();
		return sendAndParse(request, ClientDetails.class);
	}

	public void deleteClient(String id) {
		HttpRequest request = authenticatedRequestBuilder(clientsApiUrl + "/" + id).DELETE().build();
		sendAndVerify(request);
	}

	public void resetSecret(String id, String newSecret) {
		HttpRequest request = authenticatedRequestBuilder(clientsApiUrl + "/" + id + "/reset-secret")
			.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(Map.of("newSecret", newSecret))))
			.header("Content-Type", "application/json")
			.build();
		sendAndVerify(request);
	}

	public Map<String, Object> getOptions() {
		HttpRequest request = authenticatedRequestBuilder(clientsApiUrl + "/options").GET().build();
		return sendAndParse(request, new TypeReference<>() {
		});
	}

}
