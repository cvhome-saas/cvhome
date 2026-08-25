package com.asrevo.cvhome.inventory.errors;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.errors.ErrorCategory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The wire codes and the params that ride with them: checkout names the blocking sku from these, so they are part of
 * the contract, not decoration.
 */
class InventoryErrorsTest {

    private static final String SKU = "SKU-1";

    private static final String REQUESTED = "requested";

    private static final String AVAILABLE = "available";

    private static final String REF = "order-9";

    @Test
    void shortStockIsAnUnprocessableDecisionAndAnEmptyRequestIsAValidationFailure() {
        assertThat(InventoryErrors.RESERVATION_INSUFFICIENT_INVENTORY.code())
                .isEqualTo("INVENTORY.RESERVATION.INSUFFICIENT_INVENTORY");
        assertThat(InventoryErrors.RESERVATION_INSUFFICIENT_INVENTORY.category())
                .isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(InventoryErrors.RESERVATION_EMPTY.code()).isEqualTo("INVENTORY.RESERVATION.EMPTY");
        assertThat(InventoryErrors.RESERVATION_EMPTY.category()).isEqualTo(ErrorCategory.VALIDATION);
    }

    @Test
    void insufficientInventoryCarriesTheSkuAndBothQuantities() {
        InsufficientInventoryException failure = InsufficientInventoryException.of(SKU, 3, 1);

        assertThat(failure.errorCode()).isEqualTo(InventoryErrors.RESERVATION_INSUFFICIENT_INVENTORY);
        assertThat(failure.payload().detail()).contains(SKU).contains("3").contains("1");
        assertThat(failure.payload().params())
                .containsEntry("sku", SKU).containsEntry(REQUESTED, 3).containsEntry(AVAILABLE, 1);
    }

    @Test
    void notStockedReportsZeroAvailable() {
        InsufficientInventoryException failure = InsufficientInventoryException.notStocked(SKU, 2);

        assertThat(failure.payload().params()).containsEntry(AVAILABLE, 0).containsEntry(REQUESTED, 2);
    }

    @Test
    void emptyReservationNamesTheRef() {
        EmptyReservationException failure = EmptyReservationException.of(REF);

        assertThat(failure.errorCode()).isEqualTo(InventoryErrors.RESERVATION_EMPTY);
        assertThat(failure.payload().detail()).contains(REF);
        assertThat(failure.payload().params()).containsEntry("ref", REF);
    }
}
