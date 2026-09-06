package com.asrevo.cvhome.gateway.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The gateway's one advice.
 *
 * <p>
 * The shared {@code GlobalErrorHandler} is a servlet advice, and the gateway is WebFlux; until now the gateway had
 * no endpoint of its own that could fail with a typed condition. Same body, same factory, same rule — nothing but
 * {@link ProblemDetailFactory} writes a problem detail.
 * </p>
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GatewayErrorHandler {

    private final ProblemDetailFactory problems;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ProblemDetail> onBaseException(BaseException error) {
        String traceId = problems.traceId();
        ProblemDetail problem = problems.create(error, traceId);
        log.warn("[{}] {}: {}", traceId, error.payload().errorCode().code(), error.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(problem.getStatus())).body(problem);
    }

}
