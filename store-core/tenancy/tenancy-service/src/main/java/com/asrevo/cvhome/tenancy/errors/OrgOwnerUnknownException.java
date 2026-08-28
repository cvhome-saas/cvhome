package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The organization exists, but no uaa account is recorded as its owner, so there is nobody to act on.
 *
 * <p>
 * A 422 rather than a 404: the organization was found and the request was well formed — what is missing is a fact
 * about it that the caller cannot supply. Every row on the platform was in this state before
 * {@code SignupServiceImpl} began writing {@code manager_org.owner_user_id}; the backfill in
 * {@code OrgOwnerBackfill} resolves the historical ones, and this is what an organization the backfill could not
 * resolve answers with — rather than a 500, or worse, a password reset applied to nobody.
 * </p>
 */
public class OrgOwnerUnknownException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected OrgOwnerUnknownException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static OrgOwnerUnknownException of(Object orgId) {
        return new ErrorBuilder<>(TenancyErrors.ORG_OWNER_UNKNOWN, OrgOwnerUnknownException::new)
                .detail("Organization %s has no recorded owner account, so its password cannot be changed.", orgId)
                .param("orgId", String.valueOf(orgId))
                .build();
    }

}
