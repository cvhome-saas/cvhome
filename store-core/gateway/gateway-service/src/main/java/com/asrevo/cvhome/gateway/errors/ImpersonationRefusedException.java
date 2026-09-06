package com.asrevo.cvhome.gateway.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * uaa refused the exchange.
 *
 * <p>
 * The protocol error uaa answered with ({@code access_denied}, {@code invalid_grant}, …) travels as a parameter, not
 * as our code: which rule fired is uaa's audit log's to say, and the console only needs to know that it was refused
 * and what to tell the operator.
 * </p>
 */
public class ImpersonationRefusedException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ImpersonationRefusedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ImpersonationRefusedException of(String error, String description) {
        return new ErrorBuilder<>(GatewayErrors.IMPERSONATION_REFUSED, ImpersonationRefusedException::new)
                .detail(description == null ? "The identity server refused the impersonation." : description)
                .param("error", error)
                .build();
    }

}
