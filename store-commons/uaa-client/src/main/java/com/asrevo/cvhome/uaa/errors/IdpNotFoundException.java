package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/** No identity provider with that id or alias. */
public class IdpNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected IdpNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static IdpNotFoundException of(String idOrAlias) {
        return new ErrorBuilder<>(UaaErrors.IDP_NOT_FOUND, IdpNotFoundException::new)
                .detail("No identity provider exists with id or alias %s.", idOrAlias)
                .param("provider", idOrAlias)
                .build();
    }

}
