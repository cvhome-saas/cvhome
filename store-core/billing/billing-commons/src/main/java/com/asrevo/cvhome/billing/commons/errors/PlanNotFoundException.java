package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * The addressed plan is not in the catalog, or has been deactivated.
 */
public class PlanNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String PARAM_PLAN = "plan";

    protected PlanNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param code the catalog code, e.g. {@code PRO}
     */
    public static PlanNotFoundException byCode(Object code) {
        return new ErrorBuilder<>(BillingErrors.PLAN_NOT_FOUND, PlanNotFoundException::new)
                .detail("No active plan with code %s.", code)
                .param(PARAM_PLAN, code)
                .build();
    }

    /**
     * @param planId the plan a row pointed at
     */
    public static PlanNotFoundException byId(Object planId) {
        return new ErrorBuilder<>(BillingErrors.PLAN_NOT_FOUND, PlanNotFoundException::new)
                .detail("No plan %s.", planId)
                .param(PARAM_PLAN, planId)
                .build();
    }

}
