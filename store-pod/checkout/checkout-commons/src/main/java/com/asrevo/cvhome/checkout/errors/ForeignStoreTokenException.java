package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The token authenticates a shopper of a different store than the one addressed.
 *
 * <p>
 * A 403 rather than the 401 the legacy message claimed. The caller is authenticated; what they lack is a claim on this
 * store, and telling a storefront to re-authenticate would send the shopper round a loop that cannot terminate.
 * </p>
 */
public class ForeignStoreTokenException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ForeignStoreTokenException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ForeignStoreTokenException of(Object store) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_CLIENT_MISMATCH, ForeignStoreTokenException::new)
                .detail("The token does not authenticate a shopper of store %s.", store)
                .param("store", store)
                .build();
    }

}
