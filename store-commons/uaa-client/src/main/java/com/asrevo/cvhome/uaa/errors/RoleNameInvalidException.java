package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/** A role name is the token claim and the authority, so it is restricted to {@code A-Z 0-9 _}. */
public class RoleNameInvalidException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected RoleNameInvalidException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static RoleNameInvalidException of(String name) {
        return new ErrorBuilder<>(UaaErrors.ROLE_NAME_INVALID, RoleNameInvalidException::new)
                .detail("A role name is upper-case letters, digits and underscores, 2 to 80 characters.")
                .fieldError("name", UaaErrors.ROLE_NAME_INVALID, "must match ^[A-Z][A-Z0-9_]{1,79}$")
                .param("roleName", name)
                .build();
    }

}
