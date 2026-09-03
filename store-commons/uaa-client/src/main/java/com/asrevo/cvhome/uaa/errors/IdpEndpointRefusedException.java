package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * An identity-provider endpoint the server will not fetch.
 *
 * <p>
 * The reason is deliberately coarse in the message — "not allowed" rather than "resolved to 10.0.0.5". A
 * merchant who can tell a refusal caused by a private address from one caused by a bad scheme has been handed a
 * port scanner: the difference in the answer is the scan result.
 * </p>
 */
public class IdpEndpointRefusedException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected IdpEndpointRefusedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static IdpEndpointRefusedException of(String field) {
        String detail = "This endpoint is not one the server is allowed to reach. Use a public HTTPS URL.";
        return new ErrorBuilder<>(UaaErrors.IDP_ENDPOINT_REFUSED, IdpEndpointRefusedException::new)
                .detail(detail)
                .param("field", field)
                .fieldError(field, UaaErrors.IDP_ENDPOINT_REFUSED, detail)
                .build();
    }

}
