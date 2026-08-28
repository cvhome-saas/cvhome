package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * uaa returned no user for the requested id.
 *
 * <p>
 * Deliberately <em>not</em> a {@code RemoteServiceException}: uaa answered, and its answer was "no such user". That is
 * this service's own 404 to report, not a relayed failure of the call.
 * </p>
 */
public class ManagedUserNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ManagedUserNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ManagedUserNotFoundException of(String userId) {
        return of(userId, null);
    }

    /**
     * @param cause uaa's own {@code UaaUserNotFoundException}, kept so the log shows which hop reported the absence
     */
    public static ManagedUserNotFoundException of(String userId, Throwable cause) {
        return new ErrorBuilder<>(TenancyErrors.MANAGED_USER_NOT_FOUND, ManagedUserNotFoundException::new)
                .detail("No user exists with id %s.", userId)
                .param("userId", userId)
                .cause(cause)
                .build();
    }

}
