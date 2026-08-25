package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/** That address already has a live invitation to this organization. */
public class InvitationAlreadyExistsException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected InvitationAlreadyExistsException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InvitationAlreadyExistsException of(String email) {
        return new ErrorBuilder<>(TenancyErrors.INVITATION_ALREADY_EXISTS, InvitationAlreadyExistsException::new)
                .detail("%s already has a pending invitation to this organization.", email)
                .param("email", email)
                .build();
    }

}
