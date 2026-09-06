package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The shopper token was minted by another store's realm. Refused rather than silently ordering under the wrong
 * tenant.
 */
public class ForeignStoreTokenException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ForeignStoreTokenException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ForeignStoreTokenException of(Object requestedStore, Object tokenStore) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_CLIENT_MISMATCH, ForeignStoreTokenException::new)
                .detail("Token of store %s presented to store %s.", tokenStore, requestedStore)
                .param("store", requestedStore)
                .param("tokenStore", tokenStore)
                .build();
    }

}
