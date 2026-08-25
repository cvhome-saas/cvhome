package com.asrevo.cvhome.billing.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;

/**
 * Base of the failures a caller of the billing API can receive.
 *
 * <p>
 * Caller-side counterparts of billing's own exceptions, and the split is deliberate.
 * {@code SubscriptionChangeRejectedException} inside billing means <em>Stripe</em> refused billing; the types here
 * mean <em>billing</em> refused us. One shared class would make {@code remoteService} read "stripe" on one hop and
 * "billing" on the next, and no caller could tell which system actually said no.
 * </p>
 *
 * <p>
 * Catch this for "the billing API failed, however"; catch a subclass to act on a particular answer.
 * </p>
 */
public abstract class BillingApiException extends RemoteServiceException {

    /**
     * The service these failures are reported against, from this side of the call.
     */
    protected static final String BILLING_SERVICE = "billing";

    @Serial
    private static final long serialVersionUID = 1L;

    protected BillingApiException(ErrorPayload payload, Throwable cause, String remoteService, String remoteCode,
            int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

}
