package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * A store-scoped endpoint was called without the {@code store} request parameter.
 *
 * <p>
 * Every such endpoint is scoped to one tenant, so there is no sensible default to fall back on — the request cannot be
 * answered, only refused. Named separately from {@link MalformedStoreIdException} because the caller's fix differs:
 * add the parameter, rather than correct it. Renders as 400.
 * </p>
 */
public class MissingStoreParameterException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected MissingStoreParameterException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static MissingStoreParameterException of(String parameter) {
        return new ErrorBuilder<>(CommonErrors.MISSING_PARAMETER, MissingStoreParameterException::new)
                .detail("Required parameter '%s' is missing.", parameter)
                .param("parameter", parameter)
                .build();
    }

}
