package com.asrevo.cvhome.billing.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.billing.commons.errors.BillingErrors;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;

/**
 * Billing refused to let this org create another store.
 *
 * <p>
 * A definitive answer, and the caller's instruction is to stop rather than retry: the org has to settle or release
 * what it already holds first. That is what separates it from {@link BillingApiUnavailableException}, where the
 * question was never answered at all.
 * </p>
 */
public class StoreQuotaRefusedException extends BillingApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected StoreQuotaRefusedException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    /**
     * Billing answered, with a 200, that the org may not have another store.
     *
     * <p>
     * A refusal is a <em>decision</em>, not a failed call, so it comes back as a normal response carrying its reason
     * rather than as an error status — that is what lets a caller show the org why. This turns that decision into the
     * exception the caller's flow aborts on, which is why it exists alongside {@link #from(RemoteErrorContext)}: that
     * one rebuilds a refusal billing reported as an error, this one raises a refusal it reported as an answer.
     * </p>
     *
     * @param org    the org that was refused
     * @param reason billing's reason code
     */
    public static StoreQuotaRefusedException refused(Object org, String reason) {
        return RemoteServiceException.of(BillingErrors.STORE_QUOTA_EXCEEDED, StoreQuotaRefusedException::new)
                .detail("Billing refused another store for org %s: %s.", org, reason)
                .param("org", org)
                .param("reason", reason)
                .remoteService(BILLING_SERVICE)
                .remoteCode(BillingErrors.STORE_QUOTA_EXCEEDED.code())
                .remoteStatus(BillingErrors.STORE_QUOTA_EXCEEDED.category().httpStatus())
                .build();
    }

    public static StoreQuotaRefusedException from(RemoteErrorContext context) {
        return RemoteServiceException.of(BillingErrors.STORE_QUOTA_EXCEEDED, StoreQuotaRefusedException::new)
                .detail(context.detail() == null ? "Billing refused another store for this org." : context.detail())
                .params(context.params())
                .cause(context.cause())
                .remoteService(BILLING_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

}
