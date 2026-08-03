package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;
import java.util.UUID;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A mutation targeted the built-in super administrator, which no caller may disable, delete or re-role.
 *
 * <p>
 * The guard exists so the platform cannot be locked out of itself: the super admin is what grants every other
 * privilege, so an account that could remove it could remove the ability to restore it.
 * </p>
 */
public class SuperAdminImmutableException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected SuperAdminImmutableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param userId the account that was targeted — recorded because an attempt to mutate it is worth finding in a log
     */
    public static SuperAdminImmutableException of(UUID userId) {
        return new ErrorBuilder<>(UaaErrors.SUPER_ADMIN_IMMUTABLE, SuperAdminImmutableException::new)
                .detail("The super administrator account cannot be modified.")
                .param("userId", userId)
                .build();
    }

}
