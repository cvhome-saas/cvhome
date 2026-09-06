package com.asrevo.cvhome.errors.web;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.metrics.AuthRejectionMetricsFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * The three advices that sit beside {@link GlobalErrorHandler} for failures Spring raises before application code.
 *
 * <p>
 * Each one exists so a client gets a code it can branch on rather than a bare status. The one that matters most is
 * the data-integrity pair: a duplicate key and a lost optimistic lock are both conflicts, but only one of them is
 * worth retrying, so they carry different detail text under the same code. And none of the three puts the driver's
 * message in the body — a constraint name in {@code detail} leaks the schema.
 * </p>
 */
class SpecificErrorHandlersTest {

    private static final String TYPE_BASE = "https://errors.example.com";
    private static final String CONSTRAINT_TEXT = "uq_store_sku";

    private static final String RETRY_ADVICE = "reload and retry";

    private final ProblemDetailFactory factory =
            new ProblemDetailFactory(new ErrorHandlingProperties(TYPE_BASE, false));

    private static ConstraintViolation<?> violation(String path, String message) {
        ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
        Path propertyPath = Mockito.mock(Path.class);
        when(propertyPath.toString()).thenReturn(path);
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    @Test
    void aConstraintViolationBecomesPerFieldDetailUnderTheSharedCode() {
        ConstraintViolationErrorHandler handler = new ConstraintViolationErrorHandler(factory);

        ResponseEntity<ProblemDetail> response = handler.handleConstraintViolation(
                new ConstraintViolationException(Set.of(violation("create.sku", "must not be blank"))));

        assertThat(response.getBody().getProperties())
                .containsEntry(ProblemDetailFactory.CODE, CommonErrors.CONSTRAINT_VIOLATION.code())
                .containsKey(ProblemDetailFactory.FIELD_ERRORS);
        assertThat(response.getStatusCode().value())
                .isEqualTo(CommonErrors.CONSTRAINT_VIOLATION.category().httpStatus());
    }

    @Test
    void aViolationWithNoFieldsStillRendersAsTheSameCode() {
        ConstraintViolationErrorHandler handler = new ConstraintViolationErrorHandler(factory);

        ResponseEntity<ProblemDetail> response =
                handler.handleConstraintViolation(new ConstraintViolationException(Set.of()));

        assertThat(response.getBody().getProperties())
                .containsEntry(ProblemDetailFactory.CODE, CommonErrors.CONSTRAINT_VIOLATION.code());
    }

    @Test
    void aduplicateKeyAndALostLockShareACodeButNotTheirAdvice() {
        DataIntegrityErrorHandler handler = new DataIntegrityErrorHandler(factory);

        ResponseEntity<ProblemDetail> duplicate = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException(CONSTRAINT_TEXT));
        ResponseEntity<ProblemDetail> lock = handler.handleOptimisticLockingFailure(
                new OptimisticLockingFailureException("row changed"));

        assertThat(duplicate.getBody().getProperties())
                .containsEntry(ProblemDetailFactory.CODE, CommonErrors.DATA_INTEGRITY_VIOLATION.code());
        assertThat(lock.getBody().getProperties())
                .containsEntry(ProblemDetailFactory.CODE, CommonErrors.DATA_INTEGRITY_VIOLATION.code());
        // Only one of the two is worth retrying, and the detail is what says so.
        assertThat(lock.getBody().getDetail()).contains(RETRY_ADVICE);
        assertThat(duplicate.getBody().getDetail()).doesNotContain(RETRY_ADVICE);
    }

    @Test
    void theDriversMessageNeverReachesTheBody() {
        DataIntegrityErrorHandler handler = new DataIntegrityErrorHandler(factory);

        ResponseEntity<ProblemDetail> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException(CONSTRAINT_TEXT));

        // A constraint name in `detail` leaks the schema; it belongs in the log, which is where it is written.
        assertThat(response.getBody().getDetail()).doesNotContain(CONSTRAINT_TEXT);
    }

    @Test
    void accessDeniedAndUnauthenticatedAreDistinctCodesAndDistinctStatuses() {
        SecurityErrorHandler handler = new SecurityErrorHandler(factory);
        MockHttpServletRequest deniedRequest = new MockHttpServletRequest();

        ResponseEntity<ProblemDetail> denied =
                handler.handleAccessDenied(new AccessDeniedException("nope"), deniedRequest);
        ResponseEntity<ProblemDetail> unauthenticated =
                handler.handleAuthentication(new BadCredentialsException("bad"), new MockHttpServletRequest());

        assertThat(denied.getStatusCode().value()).isEqualTo(403);
        assertThat(unauthenticated.getStatusCode().value()).isEqualTo(401);
        // The rejection is named for the cvhome.auth.rejections counter, which runs outside the advice.
        assertThat(deniedRequest.getAttribute(AuthRejectionMetricsFilter.REASON_ATTRIBUTE))
                .isEqualTo("AccessDeniedException");
        assertThat(denied.getBody().getProperties())
                .containsEntry(ProblemDetailFactory.CODE, CommonErrors.ACCESS_DENIED.code());
        assertThat(unauthenticated.getBody().getProperties())
                .containsEntry(ProblemDetailFactory.CODE, CommonErrors.UNAUTHENTICATED.code());
    }

    @Test
    void neitherSecurityFailurePutsItsOwnMessageInTheBody() {
        SecurityErrorHandler handler = new SecurityErrorHandler(factory);

        // "Bad credentials for user X" in a 401 body is a user-enumeration oracle.
        assertThat(handler.handleAuthentication(new BadCredentialsException("no such user bob"),
                new MockHttpServletRequest()).getBody().getDetail()).doesNotContain("bob");
    }
}
