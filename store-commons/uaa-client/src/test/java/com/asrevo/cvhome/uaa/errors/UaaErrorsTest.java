package com.asrevo.cvhome.uaa.errors;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.asrevo.cvhome.errors.ErrorCategory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The uaa error vocabulary itself.
 *
 * <p>
 * These codes are a wire contract: consoles match on them, and the admin SDK's {@code UaaApiErrors} catalog keys its
 * caller-side translation off the same constants. A duplicated code makes two different failures indistinguishable to
 * a caller, and a code that loses its {@code UAA.} prefix stops being routable at all, so both are asserted rather
 * than left to review.
 * </p>
 */
class UaaErrorsTest {

    @ParameterizedTest
    @EnumSource(UaaErrors.class)
    void everyCodeIsPrefixedAndCategorised(UaaErrors error) {
        assertThat(error.code()).startsWith("UAA.").doesNotEndWith(".");
        assertThat(error.category()).isNotNull();
        assertThat(error.messageKey()).isEqualTo(error.code());
    }

    @Test
    void codesAreUniqueSoTwoFailuresNeverLookAlikeToACaller() {
        assertThat(Arrays.stream(UaaErrors.values()).map(UaaErrors::code).collect(Collectors.toSet()))
                .hasSameSizeAs(UaaErrors.values());
    }

    @Test
    void theCategoriesCarryTheStatusEachConditionShouldRenderAs() {
        assertThat(UaaErrors.USER_NOT_FOUND.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(UaaErrors.CLIENT_ID_TAKEN.category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(UaaErrors.CLIENT_NOT_CONFIDENTIAL.category()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(UaaErrors.SUPER_ADMIN_IMMUTABLE.category()).isEqualTo(ErrorCategory.FORBIDDEN);
        assertThat(UaaErrors.INVALID_REDIRECT_URI.category()).isEqualTo(ErrorCategory.VALIDATION);
    }

    @Test
    void aNotFoundConditionNeverRendersAsAClientError() {
        // The whole point of moving these off the deleted GeneralExceptionHandler: "user not found" was a 400.
        assertThat(UaaErrors.USER_NOT_FOUND.category().httpStatus()).isEqualTo(404);
        assertThat(UaaErrors.ROLE_NOT_FOUND.category().httpStatus()).isEqualTo(404);
        assertThat(UaaErrors.CLIENT_NOT_FOUND.category().httpStatus()).isEqualTo(404);
    }
}
