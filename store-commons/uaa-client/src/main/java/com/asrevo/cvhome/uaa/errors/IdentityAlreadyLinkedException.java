package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/** That external identity already belongs to another account. */
public class IdentityAlreadyLinkedException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected IdentityAlreadyLinkedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static IdentityAlreadyLinkedException of(String alias) {
        return new ErrorBuilder<>(UaaErrors.IDENTITY_ALREADY_LINKED, IdentityAlreadyLinkedException::new)
                .detail("That %s identity is already linked to another account.", alias)
                .param("provider", alias)
                .build();
    }

}
