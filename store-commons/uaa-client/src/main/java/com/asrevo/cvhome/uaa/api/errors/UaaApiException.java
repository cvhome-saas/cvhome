package com.asrevo.cvhome.uaa.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.uaa.service.UserAccountService;

/**
 * Base of the failures a caller of {@link UserAccountService} can receive.
 *
 * <p>
 * These are the client-SDK counterparts of uaa's own exceptions, and the distinction is the same one the payment SDK
 * draws: {@code UserNotFoundException} in {@code com.asrevo.cvhome.uaa.errors} is what uaa's {@code AdminService}
 * throws about its <em>own</em> database, while {@link UaaUserNotFoundException} here means <em>uaa told us</em> there
 * is no such user. Sharing one class for both would leave a caller unable to tell which side of the call it is
 * looking at, and would let a locally raised failure be mistaken for a relayed one.
 * </p>
 *
 * <p>
 * Catch this type for "the uaa API failed, however"; catch a subclass to act on a specific answer.
 * </p>
 */
public abstract class UaaApiException extends RemoteServiceException {

    /**
     * The service these failures are reported against, from this side of the call.
     */
    protected static final String UAA_SERVICE = "uaa";

    @Serial
    private static final long serialVersionUID = 1L;

    protected UaaApiException(ErrorPayload payload, Throwable cause, String remoteService, String remoteCode,
            int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

}
