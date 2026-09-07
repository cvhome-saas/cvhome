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
 * Mapped from {@code UAA.USER.USERNAME_TAKEN} and {@code UAA.USER.EMAIL_TAKEN} — uaa checks up front and names the
 * field — and still from {@code COMMON.DATA_INTEGRITY_VIOLATION}, the shape the unique constraint's refusal took
 * before uaa did. Unmapped, a 409 from uaa arrived as {@code UaaApiUnavailableException}: a refusal the caller could
 * act on, reported as "uaa did not complete the request", and signup answered "remote unavailable" for a taken
 * address.
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
