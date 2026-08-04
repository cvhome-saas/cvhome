package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The product exists, but it belongs to another store.
 *
 * <p>
 * 403, replacing an {@code UnauthorizedException} that answered 401. The caller <em>is</em> authenticated — the token
 * was accepted, the permission check passed — so telling them to authenticate again describes nothing they can act on
 * and sends a storefront round a login loop that cannot terminate. What they lack is a claim on this store.
 * </p>
 */
public class ForeignStoreProductAccessException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ForeignStoreProductAccessException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ForeignStoreProductAccessException of(Object productId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_FOREIGN_STORE, ForeignStoreProductAccessException::new)
                .detail("Product %s does not belong to store %s.", productId, store)
                .param("productId", productId)
                .param("store", store)
                .build();
    }

}
