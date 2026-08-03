package com.asrevo.cvhome.uaa.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;

/**
 * uaa refused a write because it conflicts with a user that already exists — a username or email already taken.
 *
 * <p>
 * Mapped from {@code COMMON.DATA_INTEGRITY_VIOLATION} rather than from a uaa-specific code, because that is genuinely
 * all uaa knows: it lets the unique constraint decide and the shared advice turns the database's refusal into a 409.
 * If uaa later checks up front and names the condition, this mapping gains a sibling and the caller-side type stays
 * the same.
 * </p>
 *
 * <p>
 * Definitive: the caller has to change the username or email, so it must not be retried unchanged.
 * </p>
 */
public class UaaConflictException extends UaaApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UaaConflictException(ErrorPayload payload, Throwable cause, String remoteService, String remoteCode,
            int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    public static UaaConflictException from(RemoteErrorContext context) {
        return RemoteServiceException.of(CommonErrors.DATA_INTEGRITY_VIOLATION, UaaConflictException::new)
                .detail(context.detail() == null ? "That user already exists in uaa." : context.detail())
                .params(context.params())
                .fieldErrors(context.fieldErrors())
                .cause(context.cause())
                .remoteService(UAA_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

}
