package com.asrevo.cvhome.inventory.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.inventory.errors.InventoryErrors;

/**
 * The inventory service refused the reservation — the stock is not there.
 *
 * <p>
 * A definitive answer, and the whole reason this type exists: it means <em>no inventory was taken</em>, so a caller
 * may fail the order and tell the shopper why. {@link InventoryApiUnavailableException} is the opposite instruction.
 * </p>
 *
 * <p>
 * The params inventory sent — {@code sku}, {@code requested}, {@code available} — are copied through, so the caller
 * can name the sku that blocked the order without a second call.
 * </p>
 */
public class ProductReservationRejectedException extends InventoryApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductReservationRejectedException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    /**
     * Rebuilds the refusal on this side from the problem body inventory sent — the entry point
     * {@code InventoryApiErrors.INVENTORY} registers for {@code INVENTORY.RESERVATION.INSUFFICIENT_INVENTORY}.
     */
    public static ProductReservationRejectedException from(RemoteErrorContext context) {
        return RemoteServiceException.of(InventoryErrors.RESERVATION_INSUFFICIENT_INVENTORY,
                        ProductReservationRejectedException::new)
                .detail(context.detail())
                .params(context.params())
                .remoteService(INVENTORY_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

}
