package com.asrevo.cvhome.merchant.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A content page or box already uses that code in this store.
 *
 * <p>
 * Previously a {@code ConstraintException} thrown inside a {@code try} whose {@code catch (Exception)} immediately
 * re-wrapped it as a generic runtime failure — so the 409 it intended never reached the client, and the check might as
 * well not have been there.
 * </p>
 */
public class DuplicateContentCodeException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateContentCodeException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicateContentCodeException of(String contentType, String code, Object store) {
        return new ErrorBuilder<>(ContentErrors.DUPLICATE_CODE, DuplicateContentCodeException::new)
                .detail("%s code %s already exists in store %s.", contentType, code, store)
                .param("contentType", contentType)
                .param("code", code)
                .param("store", store)
                .build();
    }

}
