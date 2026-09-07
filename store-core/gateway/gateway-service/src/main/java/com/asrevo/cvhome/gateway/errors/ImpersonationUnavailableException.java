package com.asrevo.cvhome.gateway.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.RemoteServiceException;

/** A service the impersonation needs did not answer — nothing was decided, and the operator is still themselves. */
public class ImpersonationUnavailableException extends RemoteServiceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ImpersonationUnavailableException(com.asrevo.cvhome.errors.ErrorPayload payload, Throwable cause,
                                                String remoteService, String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    public static ImpersonationUnavailableException of(Throwable cause, String service) {
        return RemoteServiceException.of(GatewayErrors.IMPERSONATION_UNAVAILABLE, ImpersonationUnavailableException::new)
                .detail("Service %s did not answer, so the impersonation was not started.", service)
                .cause(cause)
                .remoteService(service)
                .build();
    }

}
