package com.asrevo.cvhome.uaa.sdk;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.sdk.dto.ClientDetails;
import com.asrevo.cvhome.uaa.sdk.dto.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The admin SDK's OAuth2-client half, driven over a stubbed {@link HttpClient}.
 *
 * <p>
 * Two things are worth asserting beyond "it calls the right URL". Every request goes out with a bearer token the
 * client fetched for itself, so a caller never handles credentials — and the token request goes over the same
 * HttpClient, which is why the stub has to answer both. And a non-2xx from uaa is translated through
 * {@code UaaApiErrors}' catalog into a typed exception, so a caller can branch on "client id taken" rather than
 * on a status code; anything the catalog does not know becomes "unavailable" rather than a guess.
 * </p>
 */
class AdminClientClientTest {

    private static final String BASE_URL = "http://uaa.example.com";
    private static final String CLIENTS_PATH = "/api/v1/admin/clients";
    private static final String TOKEN_BODY = "{\"access_token\":\"tok\",\"expires_in\":3600}";
    private static final String CLIENT_ID = "c-1";
    private static final String TOKEN_PATH = "/oauth2/token";
    private static final String EMPTY_PAGE = "{\"content\":[],\"number\":0,\"size\":20,\"totalElements\":0,"
            + "\"totalPages\":0,\"last\":true,\"first\":true,\"empty\":true}";
    private static final String CLIENT_BODY = "{\"clientId\":\"%s\"}";

    private final HttpClient httpClient = Mockito.mock(HttpClient.class);
    private final List<HttpRequest> sent = new ArrayList<>();
    private AdminClientClient client;

    @SuppressWarnings("unchecked")
    private void stubbing(int apiStatus, String apiBody) throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenAnswer(call -> {
            HttpRequest request = call.getArgument(0);
            sent.add(request);
            return request.uri().getPath().endsWith(TOKEN_PATH)
                    ? response(200, TOKEN_BODY, request.uri())
                    : response(apiStatus, apiBody, request.uri());
        });
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> response(int status, String body, URI uri) {
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        when(response.uri()).thenReturn(uri);
        return response;
    }

    @BeforeEach
    void setUp() {
        client = new AdminClientClient(BASE_URL, "store-core", "secret", httpClient);
    }

    private static ClientDetails details() {
        return new ClientDetails(null, CLIENT_ID, "A client", Set.of(), Set.of(), Set.of(), Set.of(), Set.of(),
                null, null);
    }

    private HttpRequest apiRequest() {
        return sent.stream().filter(it -> it.uri().getPath().startsWith(CLIENTS_PATH)).findFirst().orElseThrow();
    }

    @Test
    void everyCallCarriesABearerTokenTheClientFetchedForItself() throws Exception {
        stubbing(200, EMPTY_PAGE);

        client.listClients(PageRequest.of(0, 20));

        // The token request goes over the same HttpClient, which is why the stub answers both.
        assertThat(sent).anyMatch(it -> it.uri().getPath().endsWith(TOKEN_PATH));
        assertThat(apiRequest().headers().firstValue("Authorization")).contains("Bearer tok");
    }

    @Test
    void apagedListingPutsItsPageAndSizeOnTheQueryString() throws Exception {
        stubbing(200, "{\"content\":[],\"number\":1,\"size\":5,\"totalElements\":0,\"totalPages\":0,"
                + "\"last\":true,\"first\":false,\"empty\":true}");

        assertThat(client.listClients(PageRequest.of(1, 5)).number()).isEqualTo(1);
        assertThat(apiRequest().uri().getQuery()).isEqualTo("page=1&size=5");
    }

    @Test
    void anUnpagedListingAsksForTheDefaultRatherThanSendingNulls() throws Exception {
        stubbing(200, EMPTY_PAGE);

        client.listClients(null);

        assertThat(apiRequest().uri().getQuery()).isNull();
    }

    @Test
    void theSingleClientReadsAndWritesAddressTheirOwnId() throws Exception {
        stubbing(200, CLIENT_BODY.formatted(CLIENT_ID));

        assertThat(client.getClient(CLIENT_ID).clientId()).isEqualTo(CLIENT_ID);
        assertThat(apiRequest().uri().getPath()).isEqualTo("%s/%s".formatted(CLIENTS_PATH, CLIENT_ID));
        assertThat(apiRequest().method()).isEqualTo("GET");
    }

    @Test
    void creatingAndUpdatingSendJsonWithTheRightVerb() throws Exception {
        stubbing(200, CLIENT_BODY.formatted(CLIENT_ID));

        client.createClient(details());
        assertThat(apiRequest().method()).isEqualTo("POST");
        assertThat(apiRequest().headers().firstValue("Content-Type")).contains("application/json");

        sent.clear();
        client.updateClient(CLIENT_ID, details());
        assertThat(apiRequest().method()).isEqualTo("PUT");
    }

    @Test
    void deletingAndResettingASecretVerifyTheResponseWithoutParsingABody() throws Exception {
        stubbing(204, "");

        client.deleteClient(CLIENT_ID);
        assertThat(apiRequest().method()).isEqualTo("DELETE");

        sent.clear();
        client.resetSecret(CLIENT_ID, "new-secret");
        assertThat(apiRequest().uri().getPath()).endsWith("/reset-secret");
    }

    @Test
    void theOptionsEndpointAnswersAMapRatherThanATypedShape() throws Exception {
        stubbing(200, "{\"grantTypes\":[\"authorization_code\"]}");

        assertThat(client.getOptions()).containsKey("grantTypes");
    }

    @Test
    void anErrorTheCatalogDoesNotKnowBecomesUnavailableRatherThanAGuess() throws Exception {
        stubbing(500, "{\"code\":\"UAA.SOMETHING.NEW\",\"detail\":\"boom\"}");

        // Recording an unknown failure as a refusal would claim uaa made a decision it never made.
        assertThatThrownBy(() -> client.getClient(CLIENT_ID))
                .isInstanceOf(UaaApiUnavailableException.class);
    }

    @Test
    void aTransportFailureIsUnavailableToo() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.io.IOException("connection refused"));

        assertThatThrownBy(() -> client.getClient(CLIENT_ID))
                .isInstanceOf(UaaApiUnavailableException.class);
    }
}
