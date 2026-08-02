package com.asrevo.cvhome.errors;

import java.io.Serial;
import java.util.Map;

/**
 * A downstream service was reachable but did not answer in time.
 *
 * <p>
 * The most dangerous of the remote failures, and the reason it is not folded into
 * {@link RemoteServiceUnavailableException}: the request very likely <em>did</em> arrive and may well have succeeded,
 * so treating it as a failure can contradict work the remote actually completed. Renders as 504 rather than 502.
 * </p>
 */
public class RemoteServiceTimeoutException extends RemoteServiceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected RemoteServiceTimeoutException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    public static RemoteServiceTimeoutException of(String service, Map<String, Object> params, Throwable cause) {
        return RemoteServiceException.of(CommonErrors.REMOTE_TIMEOUT, RemoteServiceTimeoutException::new)
                .detail("Service %s did not respond in time.", service)
                .params(params)
                .cause(cause)
                .remoteService(service)
                .build();
    }

}
