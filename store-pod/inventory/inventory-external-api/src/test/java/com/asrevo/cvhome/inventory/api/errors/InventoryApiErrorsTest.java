package com.asrevo.cvhome.inventory.api.errors;

import java.net.ConnectException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.errors.remote.RemoteExceptionFactory;
import com.asrevo.cvhome.inventory.errors.InventoryErrors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The caller-side decoding of inventory's wire codes: a refusal for lack of stock is a decision the caller may act on,
 * everything else that carries no decision arrives as "unavailable".
 */
class InventoryApiErrorsTest {

    private static final String SERVICE = "inventory";

    private static final String PATH = "/api/v1/private/reserve/order-1";

    private static final String SKU = "SKU-1";

    private static final String SHORT = "short";

    private static final String SKU_PARAM = "sku";

    private static final String EMPTY = "empty";

    private static final String PATH_PARAM = "path";

    private static RemoteServiceException decode(String code, String detail, Map<String, Object> params, int status) {
        RemoteExceptionFactory factory = InventoryApiErrors.INVENTORY.find(code).orElseThrow();
        return factory.create(new RemoteErrorContext(code, detail, params, List.of(), SERVICE, status, null, null));
    }

    @Test
    void insufficientInventoryBecomesARejectionCarryingTheSku() {
        RemoteServiceException failure = decode(InventoryErrors.RESERVATION_INSUFFICIENT_INVENTORY.code(),
                SHORT, Map.of(SKU_PARAM, SKU, "requested", 2, "available", 1), 422);

        assertThat(failure).isInstanceOf(ProductReservationRejectedException.class);
        assertThat(failure.errorCode()).isEqualTo(InventoryErrors.RESERVATION_INSUFFICIENT_INVENTORY);
        assertThat(failure.payload().detail()).isEqualTo(SHORT);
        assertThat(failure.payload().params()).containsEntry(SKU_PARAM, SKU);
        assertThat(failure.remoteService()).isEqualTo(SERVICE);
        assertThat(failure.remoteCode()).isEqualTo(InventoryErrors.RESERVATION_INSUFFICIENT_INVENTORY.code());
        assertThat(failure.remoteStatus()).isEqualTo(422);
    }

    @Test
    void emptyReservationIsOurBugAndMustNotLookLikeAStockDecision() {
        RemoteServiceException failure = decode(InventoryErrors.RESERVATION_EMPTY.code(), EMPTY, Map.of(), 400);

        assertThat(failure).isInstanceOf(InventoryApiUnavailableException.class)
                .isNotInstanceOf(ProductReservationRejectedException.class);
        assertThat(failure.errorCode()).isEqualTo(CommonErrors.REMOTE_UNAVAILABLE);
        assertThat(failure.payload().detail()).isEqualTo(EMPTY);
        assertThat(failure.remoteStatus()).isEqualTo(400);
    }

    @Test
    void aCallThatNeverArrivedIsUnavailableWithADefaultDetailAndItsCause() {
        ConnectException cause = new ConnectException("refused");
        RemoteErrorContext context = new RemoteErrorContext(null, null, Map.of(PATH_PARAM, PATH), List.of(), SERVICE, 0,
                null, cause);

        RemoteServiceException failure = InventoryApiErrors.INVENTORY.transportFailure().create(context);

        assertThat(failure).isInstanceOf(InventoryApiUnavailableException.class);
        assertThat(failure.payload().detail()).isEqualTo("The inventory service could not be reached.");
        assertThat(failure.payload().params()).containsEntry(PATH_PARAM, PATH);
        assertThat(failure.getCause()).isSameAs(cause);
        assertThat(failure.remoteStatus()).isZero();
        assertThat(context.isTransportFailure()).isTrue();
    }

    @Test
    void unnamedCodesAreNotMapped() {
        assertThat(InventoryApiErrors.INVENTORY.find("INVENTORY.SOMETHING.ELSE")).isEmpty();
    }
}
