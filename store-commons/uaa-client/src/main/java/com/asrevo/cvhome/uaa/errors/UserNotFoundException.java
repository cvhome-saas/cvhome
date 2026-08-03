package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;
import java.util.UUID;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No user exists with the requested id.
 *
 * <p>
 * Replaces {@code ResourceNotExistException}, which the deleted {@code GeneralExceptionHandler} rendered as a
 * <strong>400</strong> titled "User Not Found" — a status that said the caller's request was malformed when the
 * request was fine and the user simply was not there. The category on {@link UaaErrors#USER_NOT_FOUND} now fixes it at
 * 404, and no throw site restates a status.
 * </p>
 */
public class UserNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UserNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static UserNotFoundException of(UUID userId) {
        return new ErrorBuilder<>(UaaErrors.USER_NOT_FOUND, UserNotFoundException::new)
                .detail("No user exists with id %s.", userId)
                .param("userId", userId)
                .build();
    }

}
