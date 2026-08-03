package com.asrevo.cvhome.errors.web;

import java.util.List;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * Renders Spring Security's own failures in the shared format, so a 401 or 403 looks like every other error to a
 * client.
 *
 * <p>
 * Separate from {@link GlobalErrorHandler} so that a service without Spring Security on the classpath still gets the
 * rest of the shared error handling instead of failing to load the advice entirely.
 * </p>
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class SecurityErrorHandler {

    private static final String LOG_FORMAT = "{} [traceId={}]: {}";

    private final ProblemDetailFactory factory;

    public SecurityErrorHandler(ProblemDetailFactory factory) {
        this.factory = factory;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException exception) {
        return render(CommonErrors.ACCESS_DENIED, "Access is denied.", exception);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException exception) {
        return render(CommonErrors.UNAUTHENTICATED, "Authentication is required.", exception);
    }

    private ResponseEntity<ProblemDetail> render(ErrorCode code, String detail, Exception exception) {
        String traceId = factory.traceId();
        log.warn(LOG_FORMAT, code.code(), traceId, exception.getMessage());
        ProblemDetail problem = factory.create(code, detail, Map.of(), List.of(), traceId);
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

}
