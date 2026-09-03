package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No invitation can be used with that token — it never existed, expired, was revoked or was already accepted. One
 * exception for all four, on purpose: the endpoint that raises it is public.
 */
public class InvitationNotUsableException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected InvitationNotUsableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InvitationNotUsableException create() {
        return new ErrorBuilder<>(UaaErrors.INVITATION_NOT_USABLE, InvitationNotUsableException::new)
                .detail("This invitation is not valid. Ask for a new one.")
                .build();
    }

}
