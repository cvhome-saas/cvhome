package com.asrevo.cvhome.errors.web;

import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.FieldError;

import lombok.extern.slf4j.Slf4j;

/**
 * Renders constraint violations on method parameters and path variables with the same field-level detail
 * {@link GlobalErrorHandler} gives request bodies.
 *
 * <p>
 * Kept separate from {@link GlobalErrorHandler} so that services without bean validation on the classpath — the
 * reactive gateway, for one — can still load the shared advice.
 * </p>
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class ConstraintViolationErrorHandler {

    private final ProblemDetailFactory factory;

    public ConstraintViolationErrorHandler(ProblemDetailFactory factory) {
        this.factory = factory;
    }

    private static FieldError toFieldError(ConstraintViolation<?> violation) {
        return FieldError.of(String.valueOf(violation.getPropertyPath()), CommonErrors.CONSTRAINT_VIOLATION,
                violation.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception) {
        List<FieldError> fieldErrors = exception.getConstraintViolations().stream()
                .map(ConstraintViolationErrorHandler::toFieldError)
                .toList();

        String traceId = factory.traceId();
        log.warn("Constraint violation [traceId={}]: {}", traceId, fieldErrors);

        ProblemDetail problem = factory.create(CommonErrors.CONSTRAINT_VIOLATION, "Request validation failed.",
                Map.of(), fieldErrors, traceId);
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

}
