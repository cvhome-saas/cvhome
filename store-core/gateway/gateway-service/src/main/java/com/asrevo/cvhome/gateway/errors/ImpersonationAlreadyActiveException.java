package com.asrevo.cvhome.gateway.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/** The session is already acting as somebody; an impersonation does not nest, it is ended and started again. */
public class ImpersonationAlreadyActiveException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ImpersonationAlreadyActiveException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ImpersonationAlreadyActiveException actingAs(String username) {
        return new ErrorBuilder<>(GatewayErrors.IMPERSONATION_ALREADY_ACTIVE, ImpersonationAlreadyActiveException::new)
                .detail("This session is already acting as %s; end that first.", username)
                .param("actingAs", username)
                .build();
    }

}
