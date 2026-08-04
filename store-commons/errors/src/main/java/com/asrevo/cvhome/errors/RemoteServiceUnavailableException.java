package com.asrevo.cvhome.errors;

import java.io.Serial;
import java.util.Map;

/**
 * A downstream service could not be reached at all — connection refused, DNS failure, no route.
 *
 * <p>
 * Distinct from {@link UnmappedRemoteFailureException} in the only way that matters to a caller: <em>nothing was
 * decided</em>. The request may never have arrived, so an operation staged around it is indeterminate rather than
 * failed. Distinct from {@link RemoteServiceTimeoutException} because the remedy differs — this one points at
 * networking or a service that is not running, not at one that is merely slow.
 * </p>
 */
public class RemoteServiceUnavailableException extends RemoteServiceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected RemoteServiceUnavailableException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    public static RemoteServiceUnavailableException of(String service, Map<String, Object> params, Throwable cause) {
        return RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, RemoteServiceUnavailableException::new)
                .detail("Service %s could not be reached.", service)
                .params(params)
                .cause(cause)
                .remoteService(service)
                .build();
    }

}
