package com.asrevo.cvhome.s2s.error;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatusCode;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.RemoteServiceTimeoutException;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;
import com.asrevo.cvhome.errors.UnmappedRemoteFailureException;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.errors.remote.RemoteExceptionFactory;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Turns a downstream service's error response into the exception that service's client SDK declares — or, when it
 * declares none for that code, into a {@link RemoteServiceException} that still knows what the remote said.
 *
 * <p>
 * Before this existed, a non-2xx from a service-to-service call surfaced as a raw {@code HttpClientErrorException} that
 * nothing caught, so a remote 400 reached the browser as a local 500 carrying the remote service's stack text.
 * Preserving {@code code} and status is what makes the failure actionable; handing it to the {@link RemoteErrorCatalog}
 * is what lets a caller branch on type — {@code catch (PaymentGatewayRejectedException e)} — rather than on a string.
 * </p>
 */
@Slf4j
public final class RemoteProblemTranslator {

    private static final String CODE = "code";

    private static final String DETAIL = "detail";

    private static final String PARAMS = "params";

    private static final String FIELD_ERRORS = "fieldErrors";

    private static final String TRACE_ID = "traceId";

    private static final String FIELD = "field";

    private static final String MESSAGE = "message";

    /**
     * Response/param key naming the downstream service that failed.
     */
    private static final String SERVICE = "service";

    private static final String REMOTE_STATUS = "remoteStatus";

    private static final String PATH = "path";

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

        RemoteErrorContext context = parse(uri, status, body, null);

        return catalog.find(context.code())
                .map(factory -> factory.create(context))
                .orElseGet(() -> fallback(context, codeFor(status)));
    }

    /**
     * Exception for a call that never produced a response at all — connection refused, DNS failure, read timeout. No
     * business decision was reached, so the catalog's transport factory is used rather than a code mapping.
     */
    public static RemoteServiceException unreachable(RemoteErrorCatalog catalog, URI uri, Throwable cause) {
        String service = serviceNameOf(uri);
        Map<String, Object> params = Map.of(SERVICE, service, PATH, uri.getPath());
        RemoteErrorContext context = new RemoteErrorContext(null, null, params, List.of(), service, 0, null, cause);

        RemoteExceptionFactory factory = catalog.transportFailure();
        if (factory != null) {
            return factory.create(context);
        }
        // Reachable-but-slow and not-reachable-at-all are different remedies, so they are different types rather than
        // one type with two codes.
        return isTimeout(cause)
                ? RemoteServiceTimeoutException.of(service, params, cause)
                : RemoteServiceUnavailableException.of(service, params, cause);
    }

    /**
     * Decodes the problem body into the shape a {@link com.asrevo.cvhome.errors.remote.RemoteExceptionFactory} needs.
     * Tolerates anything: an HTML error page from a proxy, an empty body, or a JSON document of another shape.
     */
    private static RemoteErrorContext parse(URI uri, HttpStatusCode status, String body, Throwable cause) {
        String service = serviceNameOf(uri);
        String code = null;
        String detail = null;
        String traceId = null;
        Map<String, Object> params = new LinkedHashMap<>();
        List<FieldError> fieldErrors = new ArrayList<>();

        if (body != null && !body.isBlank()) {
            try {
                JsonNode root = MAPPER.readTree(body);
                code = text(root, CODE);
                detail = text(root, DETAIL);
                traceId = text(root, TRACE_ID);
                readParams(root.get(PARAMS), params);
                readFieldErrors(root.get(FIELD_ERRORS), fieldErrors);
            } catch (RuntimeException e) {
                log.debug("Remote error body from {} was not a problem document", service, e);
            }
        }

        params.putIfAbsent(SERVICE, service);
        params.putIfAbsent(PATH, uri.getPath());
        params.putIfAbsent(REMOTE_STATUS, status.value());

        return new RemoteErrorContext(code, detail, params, fieldErrors, service, status.value(), traceId, cause);
    }

    /**
     * The untyped result: still carries the remote's code, status and context, so the failure stays diagnosable even
     * for an API that publishes no catalog.
     */
    private static RemoteServiceException fallback(RemoteErrorContext context, ErrorCode errorCode) {
        String detail = context.detail() == null ? String.format("Call to %s failed.", context.service())
                : context.detail();
        return UnmappedRemoteFailureException.of(errorCode, detail, context.params(), context.fieldErrors(),
                context.service(), context.code(), context.status());
    }

    /**
     * A remote 4xx is a fault in what this service asked for and keeps its meaning; a remote 5xx is the downstream
     * service's fault, so it maps to the bad-gateway family rather than pretending this service failed.
     */
    private static ErrorCode codeFor(HttpStatusCode status) {
        if (status.value() == 504 || status.value() == 408) {
            return CommonErrors.REMOTE_TIMEOUT;
        }
        return CommonErrors.REMOTE_CALL_FAILED;
    }

    /**
     * A read timeout means the service was reachable but too slow, which is a different operational problem — and a
     * different status, 504 rather than 502 — from one that refused the connection outright.
     */
    private static boolean isTimeout(Throwable cause) {
        for (Throwable current = cause; current != null; current = current.getCause()) {
            if (current instanceof SocketTimeoutException || current instanceof HttpTimeoutException) {
                return true;
            }
        }
        return false;
    }

    private static void readParams(JsonNode node, Map<String, Object> target) {
        if (node == null || !node.isObject()) {
            return;
        }
        node.propertyStream().forEach(entry -> target.put(entry.getKey(), scalar(entry.getValue())));
    }

    private static void readFieldErrors(JsonNode node, List<FieldError> target) {
        if (node == null || !node.isArray()) {
            return;
        }
        for (JsonNode element : node) {
            String field = text(element, FIELD);
            if (field == null) {
                continue;
            }
            Map<String, Object> params = new LinkedHashMap<>();
            readParams(element.get(PARAMS), params);
            target.add(new FieldError(field, text(element, CODE), text(element, MESSAGE), params));
        }
    }

    private static Object scalar(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return node.isValueNode() ? node.asString() : node.toString();
    }

    private static String serviceNameOf(URI uri) {
        String host = uri.getHost();
        return host == null ? uri.toString() : host;
    }

    private static String text(JsonNode root, String field) {
        JsonNode node = root == null ? null : root.get(field);
        return node == null || node.isNull() ? null : node.asString();
    }

}
