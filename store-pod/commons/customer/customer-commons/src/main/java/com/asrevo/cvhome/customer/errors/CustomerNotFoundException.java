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

    private static final String STORE_PARAM = "store";

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
                .param(STORE_PARAM, store)
                .build();
    }

    /**
     * No customer carries this id. It was a {@code ResourceNotFoundException} carrying a formatted sentence and no
     * code, so the seller UI could not tell it apart from any other 404.
     */
    public static CustomerNotFoundException byId(Object customerId, Object store) {
        return new ErrorBuilder<>(CustomerErrors.CUSTOMER_NOT_FOUND, CustomerNotFoundException::new)
                .detail("No customer %s in store %s.", customerId, store)
                .param("customerId", customerId)
                .param(STORE_PARAM, store)
                .build();
    }

}
