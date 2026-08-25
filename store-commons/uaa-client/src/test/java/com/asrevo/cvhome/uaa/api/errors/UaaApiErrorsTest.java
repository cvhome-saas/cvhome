package com.asrevo.cvhome.uaa.api.errors;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.UnmappedRemoteFailureException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.errors.remote.RemoteFailures;
import com.asrevo.cvhome.uaa.errors.UaaErrors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The caller-side error contract of the uaa admin SDK.
 *
 * <p>
 * The distinction the whole catalog exists to preserve is decided versus undecided. A mapped code means uaa answered
 * and the caller can act on the answer; anything else — an unmapped code, a body that was not a problem document, a
 * call that never arrived — has to arrive as "unavailable", because recording it as a refusal would claim uaa made a
 * decision it never made.
 * </p>
 */
class UaaApiErrorsTest {

    private static final String UAA = "uaa";

    private static final String PATH = "/api/v1/admin/users/u1";

    private static final String DETAIL = "no such user";

    private static final int NOT_FOUND = 404;

    private static final int CONFLICT = 409;

    private static final String UNKNOWN_CODE = "UAA.SOMETHING.NEW";

    private static RemoteErrorContext context(String code, String detail, int status) {
        return new RemoteErrorContext(code, detail, Map.of("userId", "u1"), List.of(), UAA, status, null, null);
    }

    private static RemoteServiceException resolve(String code, int status) {
        return RemoteFailures.resolve(UaaApiErrors.CATALOG, context(code, DETAIL, status));
    }

    @Test
    void uaaSayingTheUserIsUnknownBecomesACallerSideNotFound() {
        RemoteServiceException e = resolve(UaaErrors.USER_NOT_FOUND.code(), NOT_FOUND);

        assertThat(e).isInstanceOf(UaaUserNotFoundException.class);
        assertThat(e.remoteService()).isEqualTo(UAA);
        assertThat(e.remoteCode()).isEqualTo(UaaErrors.USER_NOT_FOUND.code());
        assertThat(e.getMessage()).contains(DETAIL);
    }

    @Test
    void uaaSayingTheClientIsUnknownBecomesACallerSideNotFound() {
        assertThat(resolve(UaaErrors.CLIENT_NOT_FOUND.code(), NOT_FOUND))
                .isInstanceOf(UaaClientNotFoundException.class);
    }

    @Test
    void uaaRefusingToTouchTheSuperAdminBecomesAForbiddenOperation() {
        assertThat(resolve(UaaErrors.SUPER_ADMIN_IMMUTABLE.code(), 403))
                .isInstanceOf(UaaOperationForbiddenException.class);
    }

    /**
     * uaa has no code of its own for a duplicate user: it lets the unique constraint decide and the shared advice
     * renders the database's refusal. To a caller that still means "that user already exists".
     */
    @Test
    void theDatabasesRefusalOfADuplicateBecomesAConflict() {
        assertThat(resolve(CommonErrors.DATA_INTEGRITY_VIOLATION.code(), CONFLICT))
                .isInstanceOf(UaaConflictException.class);
    }

    @Test
    void aCodeThisSdkDoesNotNameStaysUnmappedRatherThanBeingGuessedAt() {
        assertThat(UaaApiErrors.CATALOG.find(UNKNOWN_CODE)).isEmpty();
        assertThat(UaaApiErrors.CATALOG.find(null)).isEmpty();

        RemoteServiceException e = resolve(UNKNOWN_CODE, CONFLICT);

        assertThat(e).isInstanceOf(UnmappedRemoteFailureException.class);
        assertThat(e.remoteCode()).isEqualTo(UNKNOWN_CODE);
        assertThat(e.remoteStatus()).isEqualTo(CONFLICT);
    }

    @Test
    void aCallThatNeverArrivedBecomesUnavailable() {
        RemoteServiceException e = RemoteFailures.unreachable(UaaApiErrors.CATALOG, UAA, PATH,
                new IOException("connection refused"));

        assertThat(e).isInstanceOf(UaaApiUnavailableException.class);
        assertThat(e.errorCode()).isEqualTo(CommonErrors.REMOTE_UNAVAILABLE);
        assertThat(e.getMessage()).isNotBlank();
    }

    @Test
    void anUnmappedFailureWrappedBySdkKeepsWhatUaaActuallyAnswered() {
        RemoteServiceException unmapped = resolve(UNKNOWN_CODE, CONFLICT);

        UaaApiUnavailableException wrapped = UaaApiUnavailableException.wrapping(unmapped);

        assertThat(wrapped.remoteCode()).isEqualTo(UNKNOWN_CODE);
        assertThat(wrapped.remoteStatus()).isEqualTo(CONFLICT);
        assertThat(wrapped.remoteService()).isEqualTo(UAA);
        assertThat(wrapped).hasCause(unmapped);
    }

    @Test
    void wrappingSomethingThatIsNotAnErrorOfOursStillProducesAnUndecidedFailure() {
        UaaApiUnavailableException wrapped = UaaApiUnavailableException.wrapping(new IllegalStateException("boom"));

        assertThat(wrapped.errorCode()).isEqualTo(CommonErrors.REMOTE_UNAVAILABLE);
        assertThat(wrapped.remoteCode()).isNull();
        assertThat(wrapped.getMessage()).isNotBlank();
    }

    /**
     * A failed token exchange means the request the caller asked for was never attempted. It is deliberately not an
     * authentication error: whether uaa is down or our own credentials are wrong is not something a caller can act
     * on differently.
     */
    @Test
    void aFailedTokenExchangeIsReportedAsUnavailableAndSaysWhichPhaseFailed() {
        UaaApiUnavailableException e = UaaApiUnavailableException.tokenRequestFailed("token request failed", null);

        assertThat(e.errorCode()).isEqualTo(CommonErrors.REMOTE_UNAVAILABLE);
        assertThat(e.params()).containsEntry("phase", "token").containsEntry("service", UAA);
    }
}
