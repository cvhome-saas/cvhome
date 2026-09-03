package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A grant named a role the admin API refuses to hand out.
 *
 * <p>
 * {@code SUPER_ADMIN} is the only such role: it belongs to the one seeded account, and an API that could grant it
 * would let any {@code super_admin} token mint a second platform owner. Refused loudly rather than skipped, because a
 * silent skip is indistinguishable from success.
 * </p>
 */
public class RoleNotAssignableException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected RoleNotAssignableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static RoleNotAssignableException of(String roleName) {
        return new ErrorBuilder<>(UaaErrors.ROLE_NOT_ASSIGNABLE, RoleNotAssignableException::new)
                .detail("The role %s cannot be granted through this API.", roleName)
                .param("roleName", roleName)
                .build();
    }

}
