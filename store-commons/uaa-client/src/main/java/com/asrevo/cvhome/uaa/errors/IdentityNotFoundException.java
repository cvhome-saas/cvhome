package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;
import java.util.UUID;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/** No linked identity with that id on the account. */
public class IdentityNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected IdentityNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static IdentityNotFoundException of(UUID identityId) {
        return new ErrorBuilder<>(UaaErrors.IDENTITY_NOT_FOUND, IdentityNotFoundException::new)
                .detail("No linked identity exists with id %s on this account.", identityId)
                .param("identityId", identityId)
                .build();
    }

}
