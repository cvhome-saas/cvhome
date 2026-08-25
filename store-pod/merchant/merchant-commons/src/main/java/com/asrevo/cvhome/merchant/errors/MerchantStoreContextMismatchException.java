package com.asrevo.cvhome.merchant.errors;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * The compatibility path identifies a different store from the mandatory tenant context.
 */
public class MerchantStoreContextMismatchException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected MerchantStoreContextMismatchException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static MerchantStoreContextMismatchException of(StoreMerchantId pathStore, StoreMerchantId tenantStore) {
        return new ErrorBuilder<>(MerchantErrors.STORE_CONTEXT_MISMATCH,
                MerchantStoreContextMismatchException::new)
                .detail("The store in the path does not match the tenant context.")
                .param("pathStore", pathStore)
                .param("tenantStore", tenantStore)
                .build();
    }

}
