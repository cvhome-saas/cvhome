package com.asrevo.cvhome.s2s.error;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatusCode;

import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.errors.remote.RemoteFailures;

import tools.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * The Spring clients' adapter onto {@link RemoteFailures}: decodes a response body with Jackson and hands the result
 * over, so a downstream service's error becomes the exception that service's client SDK declares.
 *
 * <p>
 * Before any of this existed, a non-2xx from a service-to-service call surfaced as a raw
 * {@code HttpClientErrorException} that nothing caught, so a remote 400 reached the browser as a local 500 carrying
 * the remote service's stack text. Preserving {@code code} and status is what makes the failure actionable; handing it
 * to the {@link RemoteErrorCatalog} is what lets a caller branch on type — {@code catch
 * (PaymentGatewayRejectedException e)} — rather than on a string.
 * </p>
 *
 * <p>
 * Only the JSON decoding lives here. Everything that decides <em>which</em> exception to build is in
 * {@code store-commons:errors}, which has no dependencies and therefore no way to parse a body — that is what lets the
 * plain {@code java.net.http} uaa SDK reach the same decisions without Spring on its classpath.
 * </p>
 */
@Slf4j
public final class RemoteProblemTranslator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RemoteProblemTranslator() {
    }

    /**
     * Builds the exception for a failed remote call.
     *
     * @param catalog the called API's error contract, passed in when the client was built; {@link
     *                RemoteErrorCatalog#none()} simply means every code falls back to the untyped form
     * @param uri     the endpoint that was called, used to name the failing service
     * @param status  the status the remote service returned
     * @param body    the raw response body; may be empty or not be a problem document at all
     */
    public static RemoteServiceException translate(RemoteErrorCatalog catalog, URI uri, HttpStatusCode status,
            String body) {

        String service = serviceNameOf(uri);
        return RemoteFailures.resolve(catalog,
                RemoteFailures.contextOf(readProblem(body, service), service, uri.getPath(), status.value(), null));
    }

    /**
     * Exception for a call that never produced a response at all — connection refused, DNS failure, read timeout. No
     * business decision was reached, so the catalog's transport factory is used rather than a code mapping.
     */
    public static RemoteServiceException unreachable(RemoteErrorCatalog catalog, URI uri, Throwable cause) {
        return RemoteFailures.unreachable(catalog, serviceNameOf(uri), uri.getPath(), cause);
    }

    /**
     * Decodes the body, or returns {@code null} when it is not a JSON object at all — an HTML error page from a proxy,
     * an empty body, a plain-text gateway message. A failed call must never fail again while being described.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> readProblem(String body, String service) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(body, Map.class);
        } catch (RuntimeException e) {
            log.debug("Remote error body from {} was not a problem document", service, e);
            return null;
        }
    }

    private static String serviceNameOf(URI uri) {
        String host = uri.getHost();
        return host == null ? uri.toString() : host;
    }

}
