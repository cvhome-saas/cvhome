package com.asrevo.cvhome.customer.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No customer in this store matches the identity on the request.
 */
public class CustomerNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CustomerNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * The shopper's {@code sub} claim resolves to no customer — the token is valid but the account it names is gone,
     * or was never provisioned in this store.
     */
    public static CustomerNotFoundException byExternalId(String cuaExternalId, Object store) {
        return new ErrorBuilder<>(CustomerErrors.CUSTOMER_NOT_FOUND, CustomerNotFoundException::new)
                .detail("No customer for sub %s in store %s.", cuaExternalId, store)
                .param("cuaExternalId", cuaExternalId)
                .param("store", store)
                .build();
    }

}
