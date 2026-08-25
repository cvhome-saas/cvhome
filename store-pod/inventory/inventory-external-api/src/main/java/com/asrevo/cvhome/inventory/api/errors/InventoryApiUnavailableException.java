package com.asrevo.cvhome.inventory.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;

/**
 * The inventory service could not be reached, or answered in a way that carried no decision — connection refused, DNS
 * failure, read timeout.
 *
 * <p>
 * The critical difference from {@link ProductReservationRejectedException}: <em>nothing was decided</em>. The stock
 * may have been reserved, or not, and this side cannot tell. A caller must leave the order recoverable rather than
 * cancelling it as out-of-stock — cancelling would both mislead the shopper and abandon inventory that a successful
 * reservation is still holding until the cleanup job expires it.
 * </p>
 */
public class InventoryApiUnavailableException extends InventoryApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected InventoryApiUnavailableException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    /**
     * Built by {@code InventoryApiErrors.INVENTORY} for a call that produced no response at all. It has no server-side
     * counterpart, because a service that never answered never threw anything.
     */
    public static InventoryApiUnavailableException from(RemoteErrorContext context) {
        return RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, InventoryApiUnavailableException::new)
                .detail(context.detail() == null ? "The inventory service could not be reached." : context.detail())
                .params(context.params())
                .cause(context.cause())
                .remoteService(INVENTORY_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

}
