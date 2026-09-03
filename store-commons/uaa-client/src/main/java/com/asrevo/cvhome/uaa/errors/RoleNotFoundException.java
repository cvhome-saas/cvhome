package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;
import java.util.UUID;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No role exists with the requested id or name.
 *
 * <p>
 * Also thrown when a grant names a role that does not exist. The previous behaviour skipped the unknown name and
 * answered 200, so a caller granting a misspelled role — or a {@code ROLE_}-prefixed authority instead of the bare
 * name — was told it had worked.
 * </p>
 */
public class RoleNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected RoleNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static RoleNotFoundException of(UUID roleId) {
        return new ErrorBuilder<>(UaaErrors.ROLE_NOT_FOUND, RoleNotFoundException::new)
                .detail("No role exists with id %s.", roleId)
                .param("roleId", roleId)
                .build();
    }

    public static RoleNotFoundException named(String name) {
        return new ErrorBuilder<>(UaaErrors.ROLE_NOT_FOUND, RoleNotFoundException::new)
                .detail("No role exists named %s.", name)
                .param("roleName", name)
                .build();
    }

}
