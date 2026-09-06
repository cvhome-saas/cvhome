package com.asrevo.cvhome.errors.web;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.errors.ErrorCodeAware;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ExternalProviderException;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.errors.RemoteServiceException;

/**
 * Builds the single error body every cvhome service returns: RFC-7807 {@link ProblemDetail} plus the extensions a
 * client needs to act on the failure rather than merely display it.
 *
 * <pre>{@code
 * {
 *   "type":     "https://errors.asrevo.com/catalog/product/not-found",
 *   "title":    "CATALOG.PRODUCT.NOT_FOUND",
 *   "status":   404,
 *   "detail":   "Product [42] does not exist in store [7]",
 *   "code":     "CATALOG.PRODUCT.NOT_FOUND",
 *   "category": "NOT_FOUND",
 *   "params":   { "productId": 42, "storeId": 7 },
 *   "fieldErrors": [ { "field": "sku", "code": "...", "message": "..." } ],
 *   "traceId":  "3f9a1c8e"
 * }
 * }</pre>
 */
public class ProblemDetailFactory {

    /**
     * Response property holding the machine-readable {@link ErrorCode#code()}.
     */
    public static final String CODE = "code";

    /**
     * Response property holding the {@link ErrorCategory} name.
     */
    public static final String CATEGORY = "category";

    /**
     * Response property holding structured context values.
     */
    public static final String PARAMS = "params";

    /**
     * Response property holding field-level validation failures.
     */
    public static final String FIELD_ERRORS = "fieldErrors";

    /**
     * Response property correlating the response with the server log entry.
     */
    public static final String TRACE_ID = "traceId";

    /**
     * Response property naming the downstream service a failure originated in.
     */
    public static final String REMOTE_SERVICE = "remoteService";

    /**
     * Response property holding the status that downstream service returned.
     */
    public static final String REMOTE_STATUS = "remoteStatus";

    /**
     * Response property naming the third party a failure originated with, e.g. {@code stripe}.
     */
    public static final String PROVIDER = "provider";

    /**
     * Response property holding the third party's own error code, e.g. {@code card_declined}.
     */
    public static final String PROVIDER_CODE = "providerCode";

    /**
     * Response property holding the status the third party returned.
     */
    public static final String PROVIDER_STATUS = "providerStatus";

    /**
     * MDC key {@code TraceContextMdcFilter} writes the current trace id under — the OpenTelemetry name, the same id
     * Loki indexes as {@code trace_id} and Tempo stores, so the response, the log line and the trace all agree.
     */
    static final String MDC_OTEL_TRACE_ID = "trace_id";

    /**
     * MDC key Micrometer Tracing used before the OTel starter became the only tracer; still honoured so a service
     * that runs with Micrometer Tracing on (tests, a future switch back) keeps its correlation.
     */
    static final String MDC_MICROMETER_TRACE_ID = TRACE_ID;

    private final ErrorHandlingProperties properties;

    public ProblemDetailFactory(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    /**
     * Body for any exception carrying an {@link ErrorCode}, which is the overwhelming majority of failures.
     */
    public ProblemDetail create(ErrorCodeAware error, String traceId) {
        ErrorPayload payload = error.payload();
        return create(payload.errorCode(), payload.detail(), payload.params(), payload.fieldErrors(), traceId);
    }

    /**
     * Body for a failure raised outside the typed hierarchy — a Spring MVC exception, or the catch-all fallback.
     */
    public ProblemDetail create(ErrorCode errorCode, String detail, Map<String, Object> params,
                                List<FieldError> fieldErrors, String traceId) {

        ProblemDetail problem = ProblemDetail.forStatus(statusOf(errorCode.category()));
        problem.setType(typeUri(errorCode));
        problem.setTitle(errorCode.code());
        if (detail != null && !detail.isBlank()) {
            problem.setDetail(detail);
        }
        problem.setProperty(CODE, errorCode.code());
        problem.setProperty(CATEGORY, errorCode.category().name());
        if (params != null && !params.isEmpty()) {
            problem.setProperty(PARAMS, params);
        }
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            problem.setProperty(FIELD_ERRORS, fieldErrors);
        }
        problem.setProperty(TRACE_ID, traceId);
        return problem;
    }

    /**
     * Overrides the status derived from the category. Needed when a remote service's status has to be preserved even
     * though the local code says something more general.
     */
    public ProblemDetail withStatus(ProblemDetail problem, HttpStatusCode status) {
        problem.setStatus(status.value());
        return problem;
    }

    /**
     * Body for a failure that happened in another service.
     *
     * <p>
     * The remote's {@code code} is re-emitted as this response's {@code code}, because that is what actually went
     * wrong: a caller of checkout that sees {@code PAYMENT.INITIATE.FAILED} can translate and act on it, whereas
     * {@code COMMON.REMOTE_CALL_FAILED} — what this used to emit — says only "a call failed somewhere". Naming the
     * origin in {@code remoteService} keeps the hop visible, and preserving the code is what lets a further hop rebuild
     * the typed exception from its catalog.
     * </p>
     */
    public ProblemDetail remote(RemoteServiceException error, HttpStatusCode status, String traceId) {
        ProblemDetail problem = create(error, traceId);
        if (error.remoteCode() != null && !error.remoteCode().isBlank()) {
            problem.setProperty(CODE, error.remoteCode());
            problem.setTitle(error.remoteCode());
        }
        if (error.remoteService() != null) {
            problem.setProperty(REMOTE_SERVICE, error.remoteService());
        }
        if (error.remoteStatus() > 0) {
            problem.setProperty(REMOTE_STATUS, error.remoteStatus());
        }
        return withStatus(problem, status);
    }

    /**
     * Body for a failure that happened at a third-party provider.
     *
     * <p>
     * The mirror image of {@link #remote}, and deliberately so. There, the remote's code and status <em>become</em> the
     * response's, because the remote is one of ours and speaks the same contract. Here they must not: a client that
     * received {@code code: "card_declined"} would be reading Stripe's catalogue as though it were ours, and a caller's
     * {@code RemoteErrorCatalog} — which keys on our code — would stop recognising the failure entirely.
     * </p>
     *
     * <p>
     * So the code, title and status stay this service's own, and the provider's values are reported alongside them as
     * {@code provider}, {@code providerCode} and {@code providerStatus}: enough for an operator to search Stripe's
     * dashboard, with no claim on our contract.
     * </p>
     */
    public ProblemDetail external(ExternalProviderException error, String traceId) {
        ProblemDetail problem = create(error, traceId);
        if (error.provider() != null) {
            problem.setProperty(PROVIDER, error.provider());
        }
        if (error.providerCode() != null && !error.providerCode().isBlank()) {
            problem.setProperty(PROVIDER_CODE, error.providerCode());
        }
        if (error.providerStatus() > 0) {
            problem.setProperty(PROVIDER_STATUS, error.providerStatus());
        }
        return problem;
    }

    /**
     * Identifier shared by the response body and the logged stack trace, so a user-reported id leads straight to the
     * server-side cause. Reuses the active tracing id when one is in the MDC, otherwise mints a short unique one.
     */
    public String traceId() {
        String fromMdc = MDC.get(MDC_OTEL_TRACE_ID);
        if (fromMdc == null || fromMdc.isBlank()) {
            fromMdc = MDC.get(MDC_MICROMETER_TRACE_ID);
        }
        if (fromMdc != null && !fromMdc.isBlank()) {
            return fromMdc;
        }
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public boolean includeDebugDetail() {
        return properties.includeDebugDetail();
    }

    private HttpStatusCode statusOf(ErrorCategory category) {
        HttpStatus resolved = HttpStatus.resolve(category.httpStatus());
        return resolved == null ? HttpStatus.INTERNAL_SERVER_ERROR : resolved;
    }

    /**
     * Turns {@code CATALOG.PRODUCT.NOT_FOUND} into {@code <base>/catalog/product/not-found}, giving each code a stable
     * documentation-shaped URI without a registry to maintain.
     */
    private URI typeUri(ErrorCode errorCode) {
        String path = errorCode.code().toLowerCase(Locale.ROOT).replace('.', '/').replace('_', '-');
        return URI.create(String.format("%s/%s", properties.typeBaseUri(), path));
    }

}
