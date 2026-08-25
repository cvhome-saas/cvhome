package com.asrevo.cvhome.merchant.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The platform's default store cannot be deleted.
 *
 * <p>
 * An {@link OperationNotAllowedException} and so a 422: the caller is permitted and the request is well-formed, but
 * the state of the data refuses it. Retrying will never help, which is the distinction a 400 lost.
 * </p>
 */
public class DefaultStoreNotRemovableException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DefaultStoreNotRemovableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DefaultStoreNotRemovableException of(Object storeMerchantId) {
        return new ErrorBuilder<>(MerchantErrors.DEFAULT_STORE_NOT_REMOVABLE, DefaultStoreNotRemovableException::new)
                .detail("The default store %s cannot be removed.", storeMerchantId)
                .param("store", storeMerchantId)
                .build();
    }

}
