package com.asrevo.cvhome.errors;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The one builder every exception type in the codebase is constructed through.
 *
 * <p>
 * Its job is to make an exception describable without making the caller defensive: passing a null map of params, or
 * no detail at all, is ordinary usage rather than a mistake, and none of it may throw while an error is being
 * raised. What the builder produces is immutable, because the payload is handed to the web layer and rendered after
 * the throw site is long gone.
 * </p>
 */
class ErrorBuilderTest {

    private static final String PRODUCT_ID = "productId";

    private static final String SKU_FIELD = "sku";

    private static final String DETAIL = "No such product";

    private static final String STORE_ID = "storeId";

    private static final String NAME_FIELD = "name";

    private static final String NOT_BLANK = "NOT_BLANK";

    private static final String MUST_NOT_BE_BLANK = "must not be blank";

    private static ErrorBuilder<NoSuchProductException> builder() {
        return new ErrorBuilder<>(CommonErrors.INTERNAL_ERROR, NoSuchProductException::new);
    }

    @Test
    void aCodeAloneIsEnoughToBuildAnException() {
        NoSuchProductException e = builder().build();

        assertThat(e.errorCode()).isEqualTo(CommonErrors.INTERNAL_ERROR);
        assertThat(e.params()).isEmpty();
        assertThat(e.fieldErrors()).isEmpty();
        assertThat(e.getMessage()).isEqualTo(CommonErrors.INTERNAL_ERROR.code());
    }

    @Test
    void theDetailIsRenderedIntoTheMessageAlongsideTheCode() {
        NoSuchProductException e = builder().detail(DETAIL).build();

        assertThat(e.getMessage()).contains(CommonErrors.INTERNAL_ERROR.code(), DETAIL);
    }

    @Test
    void aFormattedDetailIsInterpolated() {
        NoSuchProductException e = builder().detail("No product %s in store %s", 42, 7).build();

        assertThat(e.getMessage()).contains("No product 42 in store 7");
    }

    @Test
    void paramsAccumulateInTheOrderTheyWereAdded() {
        NoSuchProductException e = builder().param(PRODUCT_ID, 42).params(Map.of(STORE_ID, 7)).build();

        assertThat(e.params()).containsEntry(PRODUCT_ID, 42).containsEntry(STORE_ID, 7);
    }

    @Test
    void aNullMapOfParamsIsIgnoredRatherThanThrowingWhileAnErrorIsBeingRaised() {
        NoSuchProductException e = builder().params(null).fieldErrors(null).build();

        assertThat(e.params()).isEmpty();
        assertThat(e.fieldErrors()).isEmpty();
    }

    @Test
    void fieldErrorsCanBeAddedOneAtATimeOrInBulk() {
        NoSuchProductException e = builder()
                .fieldError(SKU_FIELD, CommonErrors.VALIDATION_FAILED, MUST_NOT_BE_BLANK)
                .fieldErrors(List.of(FieldError.of(NAME_FIELD, CommonErrors.VALIDATION_FAILED, "too long")))
                .build();

        assertThat(e.fieldErrors()).extracting(FieldError::field).containsExactly(SKU_FIELD, NAME_FIELD);
    }

    @Test
    void theCauseIsCarriedForLoggingButIsNotPartOfThePayload() {
        IllegalStateException cause = new IllegalStateException("boom");

        NoSuchProductException e = builder().cause(cause).build();

        assertThat(e).hasCause(cause);
    }

    @Test
    void whatTheBuilderProducesCannotBeMutatedAfterwards() {
        NoSuchProductException e = builder().param(PRODUCT_ID, 42).build();

        assertThatThrownBy(() -> e.params().put("other", 1)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void aPayloadWithoutACodeIsRefusedBecauseItCouldNotBeRendered() {
        assertThatThrownBy(() -> new ErrorPayload(null, DETAIL, Map.of(), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aBlankDetailFallsBackToTheCodeSoAStackTraceIsNeverAnonymous() {
        assertThat(ErrorPayload.of(CommonErrors.INTERNAL_ERROR, "   ").toMessage())
                .isEqualTo(CommonErrors.INTERNAL_ERROR.code());
        assertThat(ErrorPayload.of(CommonErrors.INTERNAL_ERROR).toMessage())
                .isEqualTo(CommonErrors.INTERNAL_ERROR.code());
    }

    @Test
    void aFieldErrorWithoutParamsStillHasAMap() {
        assertThat(FieldError.of(SKU_FIELD, NOT_BLANK, MUST_NOT_BE_BLANK).params()).isEmpty();
        assertThat(new FieldError(SKU_FIELD, NOT_BLANK, MUST_NOT_BE_BLANK, null).params()).isEmpty();
    }

    /**
     * The category bases are abstract on purpose — the codebase forbids throwing one directly — so a test needs a
     * named condition of its own, exactly as a bounded context declares one.
     */
    private static final class NoSuchProductException extends ResourceNotFoundException {

        private NoSuchProductException(ErrorPayload payload, Throwable cause) {
            super(payload, cause);
        }
    }
}
