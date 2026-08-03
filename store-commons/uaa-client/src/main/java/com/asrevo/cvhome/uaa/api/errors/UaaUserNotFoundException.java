package com.asrevo.cvhome.uaa.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.uaa.errors.UaaErrors;

/**
 * uaa answered that no user exists with the requested id.
 *
 * <p>
 * An answer, not a failure of the call: uaa was reached and reported a fact. A caller should treat it as a 404 of its
 * own — control-plane restates it as {@code ManagedUserNotFoundException} — never as "the call did not go through".
 * </p>
 */
public class UaaUserNotFoundException extends UaaApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UaaUserNotFoundException(ErrorPayload payload, Throwable cause, String remoteService, String remoteCode,
            int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    public static UaaUserNotFoundException from(RemoteErrorContext context) {
        return RemoteServiceException.of(UaaErrors.USER_NOT_FOUND, UaaUserNotFoundException::new)
                .detail(context.detail() == null ? "No such user in uaa." : context.detail())
                .params(context.params())
                .cause(context.cause())
                .remoteService(UAA_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

}
