package com.asrevo.cvhome.errors.web;

import java.util.List;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.asrevo.cvhome.errors.CommonErrors;

import lombok.extern.slf4j.Slf4j;

/**
 * Answers 409 when the database refuses a write on a constraint, instead of letting it fall through to the 500 that
 * {@link GlobalErrorHandler}'s fallback would produce.
 *
 * <p>
 * This is the behaviour uaa and cua each had in their own {@code GeneralExceptionHandler} — "this resource cannot be
 * deleted because it is in use" — kept when those two copies were deleted. It is now available to every service, so a
 * foreign-key violation stops being a 500 repo-wide.
 * </p>
 *
 * <p>
 * A backstop, not a substitute for checking: the driver's message is the only thing that says which constraint fired,
 * and it is never sent to the client. Code that knows the rule should test it and throw a condition-named exception,
 * which is what gives a client something better than "something was in use".
 * </p>
 *
 * <p>
 * Separate from {@link GlobalErrorHandler}, like the security and constraint-violation advices, so a service without
 * {@code spring-tx} on the classpath still loads the rest of the shared error handling.
 * </p>
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class DataIntegrityErrorHandler {

    private static final String DETAIL = "The request conflicts with data that already exists or is still in use.";

    private static final String LOG_FORMAT = "{} [traceId={}]";

    private final ProblemDetailFactory factory;

    public DataIntegrityErrorHandler(ProblemDetailFactory factory) {
        this.factory = factory;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        String traceId = factory.traceId();
        // Logged at warn with the stack trace: the constraint name lives in the driver's message and is the only way
        // to tell a duplicate key from a foreign-key reference after the fact.
        log.warn(LOG_FORMAT, CommonErrors.DATA_INTEGRITY_VIOLATION.code(), traceId, exception);

        ProblemDetail problem = factory.create(CommonErrors.DATA_INTEGRITY_VIOLATION, DETAIL, Map.of(), List.of(),
                traceId);
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    /**
     * A {@code @Version} guard firing means the row changed under the caller between read and commit — the same
     * "someone else got there first" a constraint violation reports, so it answers with the same 409 shape. The
     * client's move is identical too: reload and retry on fresh state.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLockingFailure(OptimisticLockingFailureException exception) {
        String traceId = factory.traceId();
        log.warn(LOG_FORMAT, CommonErrors.DATA_INTEGRITY_VIOLATION.code(), traceId, exception);
        ProblemDetail problem = factory.create(CommonErrors.DATA_INTEGRITY_VIOLATION,
                "The resource was modified concurrently; reload and retry.", Map.of(), List.of(), traceId);
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

}
