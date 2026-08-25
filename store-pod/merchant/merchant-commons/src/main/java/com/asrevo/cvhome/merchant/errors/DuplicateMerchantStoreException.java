package com.asrevo.cvhome.merchant.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A merchant store already exists with that id.
 *
 * <p>
 * A 409 rather than the 400 the legacy {@code ServiceRuntimeException} produced: the request is well-formed, the id is
 * simply taken, and a client that wants to offer "try another name" needs to tell those apart.
 * </p>
 */
public class DuplicateMerchantStoreException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateMerchantStoreException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicateMerchantStoreException of(Object storeMerchantId) {
        return new ErrorBuilder<>(MerchantErrors.DUPLICATE_STORE, DuplicateMerchantStoreException::new)
                .detail("Merchant store %s already exists.", storeMerchantId)
                .param("store", storeMerchantId)
                .build();
    }

}
