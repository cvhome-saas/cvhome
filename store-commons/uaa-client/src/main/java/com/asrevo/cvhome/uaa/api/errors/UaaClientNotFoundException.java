package com.asrevo.cvhome.uaa.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.uaa.errors.UaaErrors;

/**
 * uaa answered that no OAuth2 registered client exists with the requested id.
 *
 * <p>
 * Separate from {@link UaaUserNotFoundException} for the same reason the server keeps the two apart: the missing thing
 * is a service registration rather than a person, and the two are administered by different consoles.
 * </p>
 */
public class UaaClientNotFoundException extends UaaApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UaaClientNotFoundException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    public static UaaClientNotFoundException from(RemoteErrorContext context) {
        return RemoteServiceException.of(UaaErrors.CLIENT_NOT_FOUND, UaaClientNotFoundException::new)
                .detail(context.detail() == null ? "No such registered client in uaa." : context.detail())
                .params(context.params())
                .cause(context.cause())
                .remoteService(UAA_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

}
