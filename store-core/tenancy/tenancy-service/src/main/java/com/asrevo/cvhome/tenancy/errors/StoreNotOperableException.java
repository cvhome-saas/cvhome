package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The store cannot be worked in — it is suspended or archived, or its organization is.
 *
 * <p>
 * Distinct from billing's 402: that one means "pay and this resumes", and the merchant can act on it. This means
 * an operator closed it, and only an operator can reopen it. Rendering them the same would tell a suspended
 * merchant to go and pay, which would not help.
 * </p>
 */
public class StoreNotOperableException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected StoreNotOperableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static StoreNotOperableException of(Object storeId, String status) {
        return new ErrorBuilder<>(TenancyErrors.STORE_NOT_OPERABLE, StoreNotOperableException::new)
                .detail("Store %s is %s and cannot be used.", storeId, status)
                .param("storeId", String.valueOf(storeId))
                .param("status", status)
                .build();
    }

}
