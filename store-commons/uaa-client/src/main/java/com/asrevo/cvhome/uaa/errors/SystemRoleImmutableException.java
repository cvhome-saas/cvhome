package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The role is one the platform seeds and every service authorises on by name.
 *
 * <p>
 * Renaming {@code STORE_ADMIN} would not rename the {@code hasPermission} checks that read it, so every holder would
 * lose their access at their next sign-in; deleting it would do the same at once. Its permissions and description
 * remain editable — those are data, the name is a contract.
 * </p>
 */
public class SystemRoleImmutableException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected SystemRoleImmutableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static SystemRoleImmutableException of(String name) {
        return new ErrorBuilder<>(UaaErrors.SYSTEM_ROLE_IMMUTABLE, SystemRoleImmutableException::new)
                .detail("%s is a system role: it cannot be renamed, re-scoped or deleted.", name)
                .param("roleName", name)
                .build();
    }

}
