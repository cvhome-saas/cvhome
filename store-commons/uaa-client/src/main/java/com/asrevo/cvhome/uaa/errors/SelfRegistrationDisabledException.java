package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The realm does not accept self-registration.
 *
 * <p>
 * A refusal rather than a 404, because the endpoint exists and the caller is allowed to know the answer: a
 * storefront whose merchant has turned registration off should be able to say so rather than appear broken. The
 * platform realm keeps it off, which is why uaa shipped the setting with no endpoint behind it.
 * </p>
 */
public class SelfRegistrationDisabledException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String DETAIL = "This store is not accepting new accounts.";

    protected SelfRegistrationDisabledException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static SelfRegistrationDisabledException create() {
        return new ErrorBuilder<>(UaaErrors.SELF_REGISTRATION_DISABLED, SelfRegistrationDisabledException::new)
                .detail(DETAIL)
                .build();
    }

}
