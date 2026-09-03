package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/** A role with that name already exists. */
public class DuplicateRoleNameException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateRoleNameException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicateRoleNameException of(String name) {
        return new ErrorBuilder<>(UaaErrors.ROLE_NAME_TAKEN, DuplicateRoleNameException::new)
                .detail("A role named %s already exists.", name)
                .param("roleName", name)
                .build();
    }

}
