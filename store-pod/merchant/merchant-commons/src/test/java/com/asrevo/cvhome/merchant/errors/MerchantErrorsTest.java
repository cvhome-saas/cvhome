package com.asrevo.cvhome.merchant.errors;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.ErrorCategory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Each merchant error names its code and category: those, not a hand-picked status, decide the HTTP response, so a
 * change here is a change to the API contract.
 */
class MerchantErrorsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final StoreMerchantId OTHER = new StoreMerchantId("65f023632bc46470c104b75f");

    private static final String STORE_PARAM = "store";

    @ParameterizedTest
    @EnumSource(MerchantErrors.class)
    void everyCodeIsNamespacedToMerchant(MerchantErrors error) {
        assertThat(error.code()).startsWith("MERCHANT.");
        assertThat(error.category()).isNotNull();
    }

    @Test
    void unknownStoreIsNotFound() {
        MerchantStoreNotFoundException e = MerchantStoreNotFoundException.of(STORE);

        assertThat(e.payload().errorCode()).isEqualTo(MerchantErrors.STORE_NOT_FOUND);
        assertThat(e.payload().errorCode().category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(e.payload().params()).containsEntry(STORE_PARAM, STORE);
        assertThat(e.getMessage()).contains(STORE.getId());
    }

    @Test
    void takenIdIsAConflict() {
        DuplicateMerchantStoreException e = DuplicateMerchantStoreException.of(STORE.getId());

        assertThat(e.payload().errorCode()).isEqualTo(MerchantErrors.DUPLICATE_STORE);
        assertThat(e.payload().errorCode().category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(e.payload().params()).containsEntry(STORE_PARAM, STORE.getId());
    }

    @Test
    void defaultStoreRemovalIsUnprocessable() {
        DefaultStoreNotRemovableException e = DefaultStoreNotRemovableException.of(STORE);

        assertThat(e.payload().errorCode()).isEqualTo(MerchantErrors.DEFAULT_STORE_NOT_REMOVABLE);
        assertThat(e.payload().errorCode().category()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(e.payload().params()).containsEntry(STORE_PARAM, STORE);
    }

    @Test
    void contextMismatchNamesBothStores() {
        MerchantStoreContextMismatchException e = MerchantStoreContextMismatchException.of(STORE, OTHER);

        assertThat(e.payload().errorCode()).isEqualTo(MerchantErrors.STORE_CONTEXT_MISMATCH);
        assertThat(e.payload().errorCode().category()).isEqualTo(ErrorCategory.MALFORMED);
        assertThat(e.payload().params()).containsEntry("pathStore", STORE).containsEntry("tenantStore", OTHER);
    }


}
