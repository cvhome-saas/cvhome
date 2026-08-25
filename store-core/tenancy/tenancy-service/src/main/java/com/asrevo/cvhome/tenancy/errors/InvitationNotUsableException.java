package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The token does not match a usable invitation.
 *
 * <p>
 * One code covers "no such token", "already accepted", "revoked" and "expired" on purpose. Accepting is an
 * unauthenticated call with a bearer token, so distinguishing them would let anyone probe which tokens once
 * existed. The specific reason is logged, not returned.
 * </p>
 */
public class InvitationNotUsableException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected InvitationNotUsableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InvitationNotUsableException create() {
        return new ErrorBuilder<>(TenancyErrors.INVITATION_NOT_USABLE, InvitationNotUsableException::new)
                .detail("This invitation link is not valid, or is no longer usable.")
                .build();
    }

}
