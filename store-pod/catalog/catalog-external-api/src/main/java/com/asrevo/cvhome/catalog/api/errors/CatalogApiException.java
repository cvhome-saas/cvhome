package com.asrevo.cvhome.catalog.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;

/**
 * Base of the failures a caller of the catalog API can receive.
 *
 * <p>
 * These are the client-SDK counterparts of the catalog service's own exceptions:
 * {@code InsufficientInventoryException} in {@code catalog-commons} means catalog found the stock short, while
 * {@link ProductReservationRejectedException} here means <em>catalog refused us</em>. Catch this type for "the catalog
 * API failed, however"; catch a subclass to act on a specific answer.
 * </p>
 */
public abstract class CatalogApiException extends RemoteServiceException {

    /**
     * The service these failures are reported against, from this side of the call.
     */
    protected static final String CATALOG_SERVICE = "catalog";

    @Serial
    private static final long serialVersionUID = 1L;

    protected CatalogApiException(ErrorPayload payload, Throwable cause, String remoteService, String remoteCode,
            int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

}
