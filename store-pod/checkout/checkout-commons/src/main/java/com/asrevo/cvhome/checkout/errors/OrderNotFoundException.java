package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No order exists with that id in this store, or — when a customer asked — none belonging to them.
 */
public class OrderNotFoundException extends ResourceNotFoundException {

    private static final String ORDER_ID = "orderId";

    @Serial
    private static final long serialVersionUID = 1L;

    protected OrderNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static OrderNotFoundException of(Long orderId, Object store) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_NOT_FOUND, OrderNotFoundException::new)
                .detail("No order %s in store %s.", orderId, store)
                .param(ORDER_ID, orderId)
                .param("store", store)
                .build();
    }

    /**
     * The order exists but belongs to someone else.
     *
     * <p>
     * Deliberately the same code and status as a genuinely missing order: a 403 here would confirm the id is real,
     * which is precisely what someone walking order ids is trying to find out. The {@code customerId} param keeps the
     * distinction available in the log, where it is safe.
     * </p>
     */
    public static OrderNotFoundException forCustomer(Long orderId, Long customerId) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_NOT_FOUND, OrderNotFoundException::new)
                .detail("No order %s for customer %s.", orderId, customerId)
                .param(ORDER_ID, orderId)
                .param("customerId", customerId)
                .build();
    }

}
