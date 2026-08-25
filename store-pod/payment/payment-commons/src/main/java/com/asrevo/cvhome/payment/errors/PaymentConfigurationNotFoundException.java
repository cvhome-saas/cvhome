package com.asrevo.cvhome.payment.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * The store has no payment configuration for the requested payment type.
 *
 * <p>
 * It was a legacy {@code ResourceNotFoundException} carrying a formatted sentence and no code, so a seller updating a
 * configuration that had already been deleted got the same untyped 404 as any other missing row.
 * </p>
 */
public class PaymentConfigurationNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PaymentConfigurationNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param paymentType the payment type whose configuration was addressed
     * @param store       the store the configuration belongs to
     */
    public static PaymentConfigurationNotFoundException of(Object paymentType, Object store) {
        return new ErrorBuilder<>(PaymentErrors.CONFIGURATION_NOT_FOUND, PaymentConfigurationNotFoundException::new)
                .detail("No %s payment configuration in store %s.", paymentType, store)
                .param("paymentType", paymentType)
                .param("store", store)
                .build();
    }

}
