package com.asrevo.cvhome.uaa.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.uaa.errors.UaaErrors;

/**
 * uaa refused the mutation: the target is the built-in super administrator, which no caller may disable, delete or
 * re-role.
 *
 * <p>
 * Definitive. Retrying it unchanged will be refused again, so a caller should surface it rather than treat it as a
 * transient failure of the call.
 * </p>
 */
public class UaaOperationForbiddenException extends UaaApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UaaOperationForbiddenException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    public static UaaOperationForbiddenException from(RemoteErrorContext context) {
        return RemoteServiceException.of(UaaErrors.SUPER_ADMIN_IMMUTABLE, UaaOperationForbiddenException::new)
                .detail(context.detail() == null ? "uaa refused this operation." : context.detail())
                .params(context.params())
                .cause(context.cause())
                .remoteService(UAA_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

}
