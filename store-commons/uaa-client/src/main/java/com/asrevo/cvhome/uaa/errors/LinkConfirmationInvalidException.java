package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/** No brokered login is waiting, or the password given to confirm it did not match. One code for both, on purpose. */
public class LinkConfirmationInvalidException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FIELD = "password";

    private static final String DETAIL = "That password did not confirm the sign-in.";

    protected LinkConfirmationInvalidException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static LinkConfirmationInvalidException create() {
        return new ErrorBuilder<>(UaaErrors.LINK_CONFIRMATION_INVALID, LinkConfirmationInvalidException::new)
                .detail(DETAIL)
                .fieldError(FIELD, UaaErrors.LINK_CONFIRMATION_INVALID, DETAIL)
                .build();
    }

}
