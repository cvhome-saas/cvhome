package com.asrevo.cvhome.uaa.sdk;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.errors.remote.RemoteFailures;
import com.asrevo.cvhome.uaa.api.errors.UaaApiErrors;
import com.asrevo.cvhome.uaa.api.errors.UaaApiException;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.sdk.dto.PageResponse;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Transport for the uaa admin SDK, and the point at which uaa's problem body becomes an exception a caller can catch.
 *
 * <p>
 * Everything here used to collapse into {@code new ApiException("API call failed with status 404: {…}")} — the whole
 * response reduced to a string, so a caller could neither branch on the code nor tell a refusal from an outage. It now
 * decodes the body and resolves it against {@link UaaApiErrors#CATALOG}, which is the same contract the Spring clients
 * get from {@code RemoteErrorCatalog}; only the decoding differs, because this SDK has no Spring and speaks plain
 * {@code java.net.http}.
 * </p>
 */
public abstract class AbstractAdminClient {

    private static final RemoteErrorCatalog CATALOG = UaaApiErrors.CATALOG;

    private static final String SERVICE = "uaa";

    protected final OAuth2TokenManager tokenManager;

    protected final HttpClient httpClient;

    protected final ObjectMapper objectMapper;

    protected AbstractAdminClient(String baseUrl, String clientId, String clientSecret) {
        this(baseUrl, clientId, clientSecret, HttpClient.newBuilder().build());
    }

    /**
     * Overload taking the {@link HttpClient}, so a test can drive the error paths without a uaa to talk to.
     */
    protected AbstractAdminClient(String baseUrl, String clientId, String clientSecret, HttpClient httpClient) {
        this.tokenManager = new OAuth2TokenManager(baseUrl, clientId, clientSecret, httpClient);
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    protected HttpRequest.Builder authenticatedRequestBuilder(String url) throws UaaApiUnavailableException {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", String.format("Bearer %s", tokenManager.getAccessToken()));
    }

    protected <T> T sendAndParse(HttpRequest request, Class<T> responseType) throws UaaApiException {
        return objectMapper.readValue(send(request).body(), responseType);
    }

    protected <T> T sendAndParse(HttpRequest request, TypeReference<T> responseType) throws UaaApiException {
        return objectMapper.readValue(send(request).body(), responseType);
    }

    protected <T> PageResponse<T> sendAndParsePage(HttpRequest request, TypeReference<PageResponse<T>> typeReference)
            throws UaaApiException {
        return objectMapper.readValue(send(request).body(), typeReference);
    }

    protected void sendAndVerify(HttpRequest request) throws UaaApiException {
        send(request);
    }

    /**
     * The one place a request is executed, so the transport failures — which used to escape as
     * {@code UncheckedIOException} and {@code IllegalStateException}, describing nothing — are represented exactly
     * once.
     */
    private HttpResponse<String> send(HttpRequest request) throws UaaApiException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            verifyResponse(response);
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unreachable(request.uri(), e);
        } catch (IOException e) {
            // Interrupted and I/O-failed are one outcome to a caller: the request did not complete, so nothing about
            // it can be recorded either way.
            throw unreachable(request.uri(), e);
        }
    }

    /**
     * Turns an error response into the type {@link UaaApiErrors#CATALOG} names for its code.
     *
     * <p>
     * A code the catalog does not name comes back as {@code UnmappedRemoteFailureException}, which is not a
     * {@link UaaApiException} and so cannot be thrown from these signatures. Wrapping it says the honest thing: uaa
     * answered something this SDK has no name for, so nothing can be concluded — while the wrapper still carries
     * uaa's own code and status for whoever reads the log.
     * </p>
     */
    protected void verifyResponse(HttpResponse<String> response) throws UaaApiException {
        if (response.statusCode() < 400) {
            return;
        }
        URI uri = response.uri();
        RemoteServiceException resolved = RemoteFailures.resolve(CATALOG,
                RemoteFailures.contextOf(readProblem(response.body()), SERVICE, uri.getPath(),
                        response.statusCode(), null));

        throw resolved instanceof UaaApiException typed ? typed : UaaApiUnavailableException.wrapping(resolved);
    }

    /**
     * Decodes the body, or returns {@code null} when it is not a JSON object — an HTML page from a proxy, an empty
     * body, a plain-text gateway message. A failed call must never fail again while being described.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> readProblem(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, Map.class);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private UaaApiUnavailableException unreachable(URI uri, Throwable cause) {
        RemoteServiceException failure = RemoteFailures.unreachable(CATALOG, SERVICE, uri.getPath(), cause);
        // CATALOG names a transport type, so this always matches; the wrap is what keeps that an assertion rather
        // than an assumption.
        return failure instanceof UaaApiUnavailableException typed ? typed
                : UaaApiUnavailableException.wrapping(failure);
    }

}
