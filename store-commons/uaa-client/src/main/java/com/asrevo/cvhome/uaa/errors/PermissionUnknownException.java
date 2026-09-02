package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;
import java.util.Collection;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/** One or more permission keys are not in the catalogue. */
public class PermissionUnknownException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PermissionUnknownException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static PermissionUnknownException of(Collection<String> keys) {
        return new ErrorBuilder<>(UaaErrors.PERMISSION_UNKNOWN, PermissionUnknownException::new)
                .detail("Unknown permission(s): %s.", String.join(", ", keys))
                .param("permissions", keys)
                .build();
    }

}
