package com.asrevo.cvhome.errors.remote;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.RemoteServiceTimeoutException;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;
import com.asrevo.cvhome.errors.UnmappedRemoteFailureException;

/**
 * Turns what a downstream cvhome service reported into the exception that service's client SDK declares.
 *
 * <p>
 * Deliberately takes the problem document as a plain {@code Map} rather than parsing JSON itself, because this module
 * has no dependencies and must keep it that way — that is what lets an {@code -external-api} module publish an error
 * contract without dragging in {@code autoconfigure}. Each transport decodes the body with the JSON library it
 * already has and hands the map over: {@code RemoteProblemTranslator} for the Spring clients, and
 * {@code AbstractAdminClient} for the plain {@code java.net.http} uaa SDK. The decision-making — which is all of it —
 * happens here, once.
 * </p>
 */
public final class RemoteFailures {

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

    private RemoteFailures() {
    }

    /**
     * Reshapes a decoded problem document into what a {@link RemoteExceptionFactory} needs.
     *
     * <p>
     * Tolerates anything: {@code null} for a body that was not JSON at all — an HTML error page from a proxy, an
     * empty body — and a JSON document of some entirely different shape. A failed call must never fail again while
     * being described.
     * </p>
     *
     * @param problem the response body decoded as a map, or {@code null} when it could not be decoded
     * @param service logical name of the service that failed
     * @param path    the path that was called, recorded so a failure can be located without the full URI
     * @param status  the HTTP status the remote returned
     * @param cause   the transport failure, when there was one
     */
    public static RemoteErrorContext contextOf(Map<String, Object> problem, String service, String path, int status,
            Throwable cause) {

        Map<String, Object> params = new LinkedHashMap<>();
        List<FieldError> fieldErrors = new ArrayList<>();
        String code = null;
        String detail = null;
        String traceId = null;

        if (problem != null) {
            code = string(problem.get(CODE));
            detail = string(problem.get(DETAIL));
            traceId = string(problem.get(TRACE_ID));
            readParams(problem.get(PARAMS), params);
            readFieldErrors(problem.get(FIELD_ERRORS), fieldErrors);
        }

        // putIfAbsent, not put: when the remote named these itself its answer is the more specific one.
        params.putIfAbsent(SERVICE, service);
        params.putIfAbsent(PATH, path);
        params.putIfAbsent(REMOTE_STATUS, status);

        return new RemoteErrorContext(code, detail, params, fieldErrors, service, status, traceId, cause);
    }

    /**
     * The exception for a remote call that produced an error response: the type this API's catalog names for that
     * code, or the untyped form when it names none.
     */
    public static RemoteServiceException resolve(RemoteErrorCatalog catalog, RemoteErrorContext context) {
        return catalog.find(context.code())
                .map(factory -> factory.create(context))
                .orElseGet(() -> fallback(context, codeFor(context.status())));
    }

    /**
     * The exception for a call that never produced a response at all — connection refused, DNS failure, read timeout.
     * No business decision was reached, so the catalog's transport factory is used rather than a code mapping.
     */
    public static RemoteServiceException unreachable(RemoteErrorCatalog catalog, String service, String path,
            Throwable cause) {

        Map<String, Object> params = Map.of(SERVICE, service, PATH, path);
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
     * A remote that answers 504 or 408 timed out talking to something further down; anything else is an ordinary
     * failed call. Either way this is only the fallback code — a catalog entry overrides it.
     */
    private static ErrorCode codeFor(int status) {
        if (status == 504 || status == 408) {
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

    @SuppressWarnings("unchecked")
    private static void readParams(Object node, Map<String, Object> target) {
        if (node instanceof Map<?, ?> map) {
            ((Map<String, Object>) map).forEach(target::put);
        }
    }

    @SuppressWarnings("unchecked")
    private static void readFieldErrors(Object node, List<FieldError> target) {
        if (!(node instanceof List<?> elements)) {
            return;
        }
        for (Object element : elements) {
            if (!(element instanceof Map<?, ?> map)) {
                continue;
            }
            Map<String, Object> entry = (Map<String, Object>) map;
            String field = string(entry.get(FIELD));
            if (field == null) {
                continue;
            }
            Map<String, Object> params = new LinkedHashMap<>();
            readParams(entry.get(PARAMS), params);
            target.add(new FieldError(field, string(entry.get(CODE)), string(entry.get(MESSAGE)), params));
        }
    }

    /**
     * String form of a decoded JSON scalar. A remote that sent a number or boolean where text was expected is still
     * describable, which matters because this runs while something has already gone wrong.
     */
    private static String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

}
