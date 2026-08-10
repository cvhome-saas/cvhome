package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * The addressed price is not in the catalog, or is no longer purchasable.
 *
 * <p>
 * Deactivated prices stay readable, so an existing subscriber can still be shown what they are paying after the
 * catalog moves on. Only buying a deactivated price fails.
 * </p>
 */
public class PlanPriceNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PlanPriceNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param planPriceId the price that was addressed
     */
    public static PlanPriceNotFoundException of(Object planPriceId) {
        return new ErrorBuilder<>(BillingErrors.PLAN_PRICE_NOT_FOUND, PlanPriceNotFoundException::new)
                .detail("No purchasable price %s.", planPriceId)
                .param("planPrice", planPriceId)
                .build();
    }

}
