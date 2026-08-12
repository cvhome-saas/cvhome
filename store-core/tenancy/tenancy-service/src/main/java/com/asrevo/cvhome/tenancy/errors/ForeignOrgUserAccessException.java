package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The caller tried to administer a user belonging to another organization.
 *
 * <p>
 * The ids are recorded as params rather than only in the detail: a cross-tenant access attempt is the kind of thing
 * somebody later has to search the logs for, by either side of the boundary it crossed.
 * </p>
 *
 * <p>
 * The detail sent to the client says only that access was denied — <em>which</em> organization the user belongs to is
 * exactly what the caller is not entitled to learn.
 * </p>
 */
public class ForeignOrgUserAccessException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ForeignOrgUserAccessException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ForeignOrgUserAccessException of(String userId, String userOrg, String callerOrg) {
        return new ErrorBuilder<>(TenancyErrors.USER_FOREIGN_ORG, ForeignOrgUserAccessException::new)
                .detail("Access denied.")
                .param("userId", userId)
                .param("userOrg", userOrg)
                .param("callerOrg", callerOrg)
                .build();
    }

}
