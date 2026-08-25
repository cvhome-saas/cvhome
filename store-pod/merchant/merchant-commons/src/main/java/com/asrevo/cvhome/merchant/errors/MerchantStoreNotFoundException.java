package com.asrevo.cvhome.merchant.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No merchant store exists for that id.
 */
public class MerchantStoreNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected MerchantStoreNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static MerchantStoreNotFoundException of(Object storeMerchantId) {
        return new ErrorBuilder<>(MerchantErrors.STORE_NOT_FOUND, MerchantStoreNotFoundException::new)
                .detail("No merchant store %s.", storeMerchantId)
                .param("store", storeMerchantId)
                .build();
    }

}
