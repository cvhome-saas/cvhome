package com.asrevo.cvhome.errors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The category is what turns an error code into an HTTP status, so it is the wire contract every client sees.
 *
 * <p>
 * A code names <em>what kind</em> of failure occurred and never a raw status number, which means a category whose
 * status changed would silently re-map every code that uses it. These assertions are here to make that loud.
 * </p>
 */
class ErrorCategoryTest {

    @Test
    void eachCategoryMapsToTheStatusClientsAlreadyDependOn() {
        assertThat(ErrorCategory.VALIDATION.httpStatus()).isEqualTo(400);
        assertThat(ErrorCategory.UNAUTHENTICATED.httpStatus()).isEqualTo(401);
        assertThat(ErrorCategory.FORBIDDEN.httpStatus()).isEqualTo(403);
        assertThat(ErrorCategory.NOT_FOUND.httpStatus()).isEqualTo(404);
        assertThat(ErrorCategory.CONFLICT.httpStatus()).isEqualTo(409);
        assertThat(ErrorCategory.PAYLOAD_TOO_LARGE.httpStatus()).isEqualTo(413);
        assertThat(ErrorCategory.UNPROCESSABLE.httpStatus()).isEqualTo(422);
        assertThat(ErrorCategory.INTERNAL.httpStatus()).isEqualTo(500);
        assertThat(ErrorCategory.REMOTE_SERVICE.httpStatus()).isEqualTo(502);
        assertThat(ErrorCategory.TIMEOUT.httpStatus()).isEqualTo(504);
    }

    @ParameterizedTest
    @EnumSource(ErrorCategory.class)
    void aCategoryIsAClientErrorExactlyWhenItsStatusIs4xx(ErrorCategory category) {
        assertThat(category.isClientError())
                .isEqualTo(category.httpStatus() >= 400 && category.httpStatus() < 500);
    }

    @ParameterizedTest
    @EnumSource(CommonErrors.class)
    void everyCommonCodeIsNamespacedAndCarriesACategory(CommonErrors error) {
        assertThat(error.code()).startsWith("COMMON.");
        assertThat(error.category()).isNotNull();
    }

    @Test
    void theRemoteCodesKeepTheStatusesTheClientSdksMapOn() {
        assertThat(CommonErrors.REMOTE_CALL_FAILED.category()).isEqualTo(ErrorCategory.REMOTE_SERVICE);
        assertThat(CommonErrors.REMOTE_TIMEOUT.category()).isEqualTo(ErrorCategory.TIMEOUT);
    }
}
