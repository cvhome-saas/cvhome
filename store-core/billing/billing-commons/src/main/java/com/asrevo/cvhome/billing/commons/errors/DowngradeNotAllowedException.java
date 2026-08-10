package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;
import java.util.Collection;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The target plan's ceilings are already exceeded by what the store holds today, so the downgrade is refused rather
 * than scheduled.
 *
 * <p>
 * Raised when a downgrade is <em>requested</em>. It is deliberately not raised when a scheduled downgrade comes due:
 * by then Stripe has already moved the customer to the cheaper price, and refusing would leave the two systems
 * disagreeing about what is being paid for. The cheaper ceilings simply start applying to new writes, and the data
 * already there stays readable — nothing is ever deleted to make a plan fit.
 * </p>
 */
public class DowngradeNotAllowedException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DowngradeNotAllowedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param store     the store whose subscription was addressed
     * @param plan      the plan that was asked for
     * @param exceeded  the entitlement keys the store is already over
     */
    public static DowngradeNotAllowedException of(Object store, Object plan, Collection<?> exceeded) {
        return new ErrorBuilder<>(BillingErrors.DOWNGRADE_NOT_ALLOWED, DowngradeNotAllowedException::new)
                .detail("Store %s already exceeds plan %s on %s.", store, plan, exceeded)
                .param("store", store)
                .param("plan", plan)
                .param("exceeded", exceeded)
                .build();
    }

}
