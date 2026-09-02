package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;
import java.util.UUID;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/** The account already holds a live invitation; a resend rotates it rather than adding a second. */
public class InvitationAlreadyPendingException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected InvitationAlreadyPendingException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InvitationAlreadyPendingException of(UUID userId) {
        return new ErrorBuilder<>(UaaErrors.INVITATION_ALREADY_PENDING, InvitationAlreadyPendingException::new)
                .detail("The account already has a pending invitation; resend it to issue a new link.")
                .param("userId", userId)
                .build();
    }

}
