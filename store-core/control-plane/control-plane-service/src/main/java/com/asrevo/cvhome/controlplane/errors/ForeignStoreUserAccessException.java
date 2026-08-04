package com.asrevo.cvhome.controlplane.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The caller tried to administer a user belonging to another store within the same organization.
 *
 * <p>
 * Separate from {@link ForeignOrgUserAccessException} because the two say different things about the caller: crossing
 * an org boundary is somebody reaching into another tenant, while crossing a store boundary is usually a legitimate
 * administrator with the wrong {@code store} on the request. Same 403, different investigation.
 * </p>
 */
public class ForeignStoreUserAccessException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ForeignStoreUserAccessException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ForeignStoreUserAccessException of(String userId, String userStore, String requestedStore) {
        return new ErrorBuilder<>(ControlPlaneErrors.USER_FOREIGN_STORE, ForeignStoreUserAccessException::new)
                .detail("Access denied.")
                .param("userId", userId)
                .param("userStore", userStore)
                .param("requestedStore", requestedStore)
                .build();
    }

}
