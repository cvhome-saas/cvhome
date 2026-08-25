package com.asrevo.cvhome.errors;

import java.io.Serial;
import java.util.Collection;
import java.util.Map;

/**
 * A downstream service answered with an error this codebase has no type for.
 *
 * <p>
 * "We have no name for this" is itself a condition with a name, which is what lets {@link RemoteServiceException} stay
 * abstract like every other category base. Before this existed, the base was concrete purely so the translator had
 * something to build — an exemption that also left the generic type throwable from anywhere.
 * </p>
 *
 * <p>
 * It still carries the remote's {@code code}, status and params, so an unmapped failure is diagnosable even though it
 * is not actionable by type. A caller that needs to branch on one of these should add it to that API's
 * {@link com.asrevo.cvhome.errors.remote.RemoteErrorCatalog}, which is precisely the signal this type gives.
 * </p>
 */
public class UnmappedRemoteFailureException extends RemoteServiceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UnmappedRemoteFailureException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    public static Builder<UnmappedRemoteFailureException> of(ErrorCode errorCode) {
        return RemoteServiceException.of(errorCode, UnmappedRemoteFailureException::new);
    }

    /**
     * Convenience for the common shape: everything the remote reported, under the given code.
     */
    public static UnmappedRemoteFailureException of(ErrorCode errorCode, String detail, Map<String, Object> params,
            Collection<FieldError> fieldErrors, String service, String remoteCode, int remoteStatus) {
        return of(errorCode)
                .detail(detail)
                .params(params)
                .fieldErrors(fieldErrors)
                .remoteService(service)
                .remoteCode(remoteCode)
                .remoteStatus(remoteStatus)
                .build();
    }

}
