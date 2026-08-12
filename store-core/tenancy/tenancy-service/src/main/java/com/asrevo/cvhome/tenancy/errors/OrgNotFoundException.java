package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/** No organization with the requested id is visible to the caller. */
public class OrgNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected OrgNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static OrgNotFoundException of(Object orgId) {
        return new ErrorBuilder<>(TenancyErrors.ORG_NOT_FOUND, OrgNotFoundException::new)
                .detail("No organization is visible with id %s.", orgId)
                .param("orgId", String.valueOf(orgId))
                .build();
    }

}
