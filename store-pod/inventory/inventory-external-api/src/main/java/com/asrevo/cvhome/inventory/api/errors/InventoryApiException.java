package com.asrevo.cvhome.inventory.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;

/**
 * Base of the failures a caller of the inventory API can receive.
 *
 * <p>
 * These are the client-SDK counterparts of the inventory service's own exceptions:
 * {@code InsufficientInventoryException} in {@code inventory-commons} means inventory found the stock short, while
 * {@link ProductReservationRejectedException} here means <em>inventory refused us</em>. Catch this type for "the
 * inventory API failed, however"; catch a subclass to act on a specific answer.
 * </p>
 */
public abstract class InventoryApiException extends RemoteServiceException {

    /**
     * The service these failures are reported against, from this side of the call.
     */
    protected static final String INVENTORY_SERVICE = "inventory";

    @Serial
    private static final long serialVersionUID = 1L;

    protected InventoryApiException(ErrorPayload payload, Throwable cause, String remoteService, String remoteCode,
            int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

}
