package com.asrevo.cvhome.uaa.sdk;

import java.net.http.HttpRequest;
import java.util.Map;

import com.asrevo.cvhome.uaa.api.errors.UaaApiException;
import com.asrevo.cvhome.uaa.sdk.dto.ClientDetails;
import com.asrevo.cvhome.uaa.sdk.dto.ClientSummary;
import com.asrevo.cvhome.uaa.sdk.dto.PageRequest;
import com.asrevo.cvhome.uaa.sdk.dto.PageResponse;

import tools.jackson.core.type.TypeReference;

public class AdminClientClient extends AbstractAdminClient {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    private static final String PATH_SEPARATOR = "/";

    private static final String PATH_WITH_ID_TEMPLATE = "%s%s%s";

    private static final String API_PATH = "%s/api/v1/admin/clients";

    private final String clientsApiUrl;

    public AdminClientClient(String baseUrl, String clientId, String clientSecret) {
        super(baseUrl, clientId, clientSecret);
        this.clientsApiUrl = String.format(API_PATH, baseUrl);
    }

    /**
     * Overload taking the {@link java.net.http.HttpClient}, so a test can exercise the error paths without a uaa to
     * talk to.
     */
    public AdminClientClient(String baseUrl, String clientId, String clientSecret, java.net.http.HttpClient httpClient) {
        super(baseUrl, clientId, clientSecret, httpClient);
        this.clientsApiUrl = String.format(API_PATH, baseUrl);
    }

    public PageResponse<ClientSummary> listClients(PageRequest pageRequest) throws UaaApiException {
        String url = clientsApiUrl;
        if (pageRequest != null) {
            url += String.format("?page=%d&size=%d", pageRequest.page(), pageRequest.size());
        }
        HttpRequest request = authenticatedRequestBuilder(url).GET().build();
        return sendAndParsePage(request, new TypeReference<>() {
        });
    }

    public ClientDetails getClient(String id) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(
                String.format(PATH_WITH_ID_TEMPLATE, clientsApiUrl, PATH_SEPARATOR, id)).GET().build();
        return sendAndParse(request, ClientDetails.class);
    }

    public ClientDetails createClient(ClientDetails req) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(clientsApiUrl)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        return sendAndParse(request, ClientDetails.class);
    }

    public ClientDetails updateClient(String id, ClientDetails req) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format(PATH_WITH_ID_TEMPLATE, clientsApiUrl, PATH_SEPARATOR, id))
                .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(req)))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        return sendAndParse(request, ClientDetails.class);
    }

    public void deleteClient(String id) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(
                String.format(PATH_WITH_ID_TEMPLATE, clientsApiUrl, PATH_SEPARATOR, id)).DELETE().build();
        sendAndVerify(request);
    }

    public void resetSecret(String id, String newSecret) throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s%s%s/reset-secret", clientsApiUrl, PATH_SEPARATOR, id))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(Map.of("newSecret", newSecret))))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .build();
        sendAndVerify(request);
    }

    public Map<String, Object> getOptions() throws UaaApiException {
        HttpRequest request = authenticatedRequestBuilder(String.format("%s/options", clientsApiUrl)).GET().build();
        return sendAndParse(request, new TypeReference<>() {
        });
    }

}
