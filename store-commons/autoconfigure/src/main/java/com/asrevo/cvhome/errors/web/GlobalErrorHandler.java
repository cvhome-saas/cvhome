package com.asrevo.cvhome.errors.web;

import java.util.List;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.errors.ErrorCodeAware;
import com.asrevo.cvhome.errors.ExternalProviderException;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.UncheckedBaseException;

import lombok.extern.slf4j.Slf4j;

/**
 * The single error-rendering layer for every cvhome service.
 *
 * <p>
 * Registered with no {@code basePackages} restriction, deliberately: the advice this replaces was scoped to
 * {@code com.asrevo.cvhome.store.controller} while every real API lives in {@code com.asrevo.cvhome.<service>.api.v1},
 * so it never applied to the controllers it was written for and failures fell through to Spring's default 500.
 * </p>
 *
 * <p>
 * Runs at {@link Ordered#LOWEST_PRECEDENCE} so a service can still register a more specific advice and win.
 * </p>
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalErrorHandler extends ResponseEntityExceptionHandler {

    private static final String INTERNAL_DETAIL = "The request could not be completed. Quote the traceId when reporting this error.";

    private static final String CLIENT_ERROR_LOG = "{} [traceId={}]: {}";

    private final ProblemDetailFactory factory;

    public GlobalErrorHandler(ProblemDetailFactory factory) {
        this.factory = factory;
    }

    private static boolean hasCode(ProblemDetail problem) {
        return problem.getProperties() != null && problem.getProperties().containsKey(ProblemDetailFactory.CODE);
    }

    private static String detailOf(Object body) {
        return body instanceof ProblemDetail problem ? problem.getDetail() : null;
    }

    /**
     * Gives Spring's own MVC failures a code from the shared vocabulary, so a client can branch on them the same way it
     * branches on application errors.
     */
    private static ErrorCode codeFor(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 405 -> CommonErrors.METHOD_NOT_ALLOWED;
            case 413 -> CommonErrors.UPLOAD_TOO_LARGE;
            case 415 -> CommonErrors.UNSUPPORTED_MEDIA_TYPE;
            case 400 -> CommonErrors.MALFORMED_REQUEST;
            case 404 -> CommonErrors.RESOURCE_NOT_FOUND;
            default -> CommonErrors.INTERNAL_ERROR;
        };
    }

    private static FieldError toFieldError(org.springframework.validation.FieldError source) {
        return new FieldError(source.getField(), String.format("VALIDATION.%s", source.getCode()),
                source.getDefaultMessage(), Map.of());
    }

    private static String rootCauseMessage(Throwable exception) {
        Throwable root = exception;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() == null ? root.getClass().getName() : root.getMessage();
    }

    /**
     * Every typed failure raised by application code.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ProblemDetail> handleBaseException(BaseException exception) {
        return render(exception, exception);
    }

    /**
     * A checked failure that crossed a lambda or callback boundary inside {@code UncheckedBaseException}. Unwrapped
     * here so a carrier that escapes still produces the status its cause intended, rather than a 500.
     */
    @ExceptionHandler(UncheckedBaseException.class)
    public ResponseEntity<ProblemDetail> handleUncheckedBaseException(UncheckedBaseException exception) {
        return render(exception.getCause(), exception);
    }

    /**
     * Bean validation on a request body. Spring's own handling returns a bare 400; this adds the per-field detail a
     * client needs to highlight the offending controls.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(GlobalErrorHandler::toFieldError)
                .toList();

        String traceId = factory.traceId();
        log.warn("Validation failed [traceId={}] on {}: {}", traceId, exception.getObjectName(), fieldErrors);

        ProblemDetail problem = factory.create(CommonErrors.VALIDATION_FAILED, "Request validation failed.", Map.of(),
                fieldErrors, traceId);
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    /**
     * The single funnel for every exception {@link ResponseEntityExceptionHandler} handles itself — unreadable bodies,
     * missing parameters, unsupported media types, oversized uploads and the rest.
     *
     * <p>
     * Rewriting the body here rather than adding a handler per exception means those all gain a {@code code} and a
     * {@code traceId} for free, and it is why the four per-service copies of the upload advice can be deleted.
     * </p>
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {

        if (body instanceof ProblemDetail problem && hasCode(problem)) {
            return super.handleExceptionInternal(exception, body, headers, statusCode, request);
        }

        String traceId = factory.traceId();
        log.warn(CLIENT_ERROR_LOG, statusCode, traceId, exception.getMessage());

        ProblemDetail problem = factory.create(codeFor(statusCode), detailOf(body), Map.of(), List.of(), traceId);
        factory.withStatus(problem, statusCode);
        return super.handleExceptionInternal(exception, problem, headers, statusCode, request);
    }

    /**
     * Anything unclassified. The client gets a code, a status and a traceId — never the root-cause text, which is what
     * the handler this replaces concatenated into the response body.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception) {
        // Catches any unchecked exception that carries a code without this module having to know its type — which is
        // how the deprecated hierarchy renders correctly while its throw sites are migrated module by module.
        if (exception instanceof ErrorCodeAware aware) {
            return render(aware, exception);
        }

        String traceId = factory.traceId();
        log.error("Unhandled failure [traceId={}]", traceId, exception);

        String detail = factory.includeDebugDetail() ? rootCauseMessage(exception) : INTERNAL_DETAIL;
        ProblemDetail problem = factory.create(CommonErrors.INTERNAL_ERROR, detail, Map.of(), List.of(), traceId);
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    /**
     * Shared rendering path. Server-side faults are logged with their stack trace; client faults are logged without
     * one, because a user asking for a missing product is not an incident.
     */
    private ResponseEntity<ProblemDetail> render(ErrorCodeAware error, Exception thrown) {
        String traceId = factory.traceId();
        ErrorCategory category = error.category();

        if (category.isClientError()) {
            log.warn(CLIENT_ERROR_LOG, error.errorCode().code(), traceId, thrown.getMessage());
        } else {
            log.error("{} [traceId={}]", error.errorCode().code(), traceId, thrown);
        }

        ProblemDetail problem = renderBody(error, traceId);
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    /**
     * Three shapes, chosen by where the failure happened: a peer cvhome service, whose code and status we adopt; a
     * third-party provider, whose code and status we only report; or this service, which needs neither.
     */
    private ProblemDetail renderBody(ErrorCodeAware error, String traceId) {
        if (error instanceof RemoteServiceException remote) {
            return factory.remote(remote, remoteStatus(remote), traceId);
        }
        if (error instanceof ExternalProviderException provider) {
            // No status override: a provider's status describes a conversation the client was never part of, so the
            // status here comes from our own category, as it does for any locally raised failure.
            return factory.external(provider, traceId);
        }
        return factory.create(error, traceId);
    }

    /**
     * A downstream 4xx is the caller's problem and keeps its status; a downstream 5xx becomes a 502, because this
     * service is not the one that failed.
     */
    private HttpStatusCode remoteStatus(RemoteServiceException remote) {
        HttpStatus status = HttpStatus.resolve(remote.remoteStatus());
        if (status != null && status.is4xxClientError()) {
            return status;
        }
        return HttpStatus.BAD_GATEWAY;
    }

}
