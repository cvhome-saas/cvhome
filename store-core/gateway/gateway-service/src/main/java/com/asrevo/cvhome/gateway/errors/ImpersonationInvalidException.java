package com.asrevo.cvhome.gateway.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/** A start request missing one of the four things an impersonation needs: who, where, how, and why. */
public class ImpersonationInvalidException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ImpersonationInvalidException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ImpersonationInvalidException missing(String field) {
        return new ErrorBuilder<>(GatewayErrors.IMPERSONATION_INVALID, ImpersonationInvalidException::new)
                .detail("'%s' is required.", field)
                .param("field", field)
                .build();
    }

}
