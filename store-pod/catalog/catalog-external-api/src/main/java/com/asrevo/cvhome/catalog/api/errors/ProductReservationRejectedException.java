package com.asrevo.cvhome.catalog.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.catalog.errors.CatalogErrors;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;

/**
 * The catalog service refused the reservation — the stock is not there.
 *
 * <p>
 * A definitive answer, and the whole reason this type exists: it means <em>no inventory was taken</em>, so a caller
 * may fail the order and tell the shopper why. {@link CatalogApiUnavailableException} is the opposite instruction, and
 * before this split both arrived at {@code OrderInventoryOrchestratorImpl} as a bare {@code catch (Exception)} that
 * turned into {@code status(false)} — "out of stock" and "catalog is down" were the same outcome, and a shopper was
 * told an item was unavailable whenever a deploy restarted catalog.
 * </p>
 *
 * <p>
 * The params catalog sent — {@code sku}, {@code requested}, {@code available} — are copied through, so the caller can
 * name the sku that blocked the order without a second call.
 * </p>
 */
public class ProductReservationRejectedException extends CatalogApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductReservationRejectedException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    /**
     * Rebuilds the refusal on this side from the problem body catalog sent — the entry point
     * {@code CatalogApiErrors.CATALOG} registers for {@code CATALOG.RESERVATION.INSUFFICIENT_INVENTORY}.
     */
    public static ProductReservationRejectedException from(RemoteErrorContext context) {
        return RemoteServiceException.of(CatalogErrors.RESERVATION_INSUFFICIENT_INVENTORY,
                        ProductReservationRejectedException::new)
                .detail(context.detail())
                .params(context.params())
                .remoteService(CATALOG_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

}
