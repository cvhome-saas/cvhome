package com.asrevo.cvhome.checkout.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;

/**
 * Base of the failures a caller of {@code ExternalOrderSignalService} can receive: checkout, seen from payment's or
 * inventory's side of the call.
 */
public abstract class CheckoutApiException extends RemoteServiceException {

    protected static final String CHECKOUT_SERVICE = "checkout";

    @Serial
    private static final long serialVersionUID = 1L;

    protected CheckoutApiException(ErrorPayload payload, Throwable cause, String remoteService, String remoteCode,
                                   int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

}
