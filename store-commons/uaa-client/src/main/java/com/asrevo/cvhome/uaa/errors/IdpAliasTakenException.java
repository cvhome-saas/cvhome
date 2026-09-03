package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/** The alias is the registration id every redirect URI carries; two providers cannot share one. */
public class IdpAliasTakenException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FIELD = "alias";

    private static final String DETAIL = "That alias is already used by another provider.";

    protected IdpAliasTakenException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static IdpAliasTakenException of(String alias) {
        return new ErrorBuilder<>(UaaErrors.IDP_ALIAS_TAKEN, IdpAliasTakenException::new)
                .detail(DETAIL)
                .param(FIELD, alias)
                .fieldError(FIELD, UaaErrors.IDP_ALIAS_TAKEN, DETAIL)
                .build();
    }

}
