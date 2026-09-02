package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/** The role is still assigned; deleting it would silently strip those accounts of authority. */
public class RoleInUseException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected RoleInUseException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static RoleInUseException of(String name, long holders) {
        return new ErrorBuilder<>(UaaErrors.ROLE_IN_USE, RoleInUseException::new)
                .detail("%s is held by %d account(s); remove it from them first.", name, holders)
                .param("roleName", name)
                .param("holders", holders)
                .build();
    }

}
