package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No order exists with that id or ref in this store, or — when a shopper asked — none belonging to them.
 *
 * <p>
 * The shopper case is deliberately the same code and status as a genuinely missing order: a 403 would confirm the
 * id is real, which is precisely what someone walking order ids is trying to find out.
 * </p>
 */
public class OrderNotFoundException extends ResourceNotFoundException {

    private static final String ORDER_ID = "orderId";

    private static final String STORE = "store";

    @Serial
    private static final long serialVersionUID = 1L;

    protected OrderNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static OrderNotFoundException of(Long orderId, Object store) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_NOT_FOUND, OrderNotFoundException::new)
                .detail("No order %s in store %s.", orderId, store)
                .param(ORDER_ID, orderId)
                .param(STORE, store)
                .build();
    }

    public static OrderNotFoundException ofRef(String orderRef, Object store) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_NOT_FOUND, OrderNotFoundException::new)
                .detail("No order with ref %s in store %s.", orderRef, store)
                .param("orderRef", orderRef)
                .param(STORE, store)
                .build();
    }

    public static OrderNotFoundException forShopper(Long orderId, Object shopper) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_NOT_FOUND, OrderNotFoundException::new)
                .detail("No order %s for shopper %s.", orderId, shopper)
                .param(ORDER_ID, orderId)
                .param("shopper", shopper)
                .build();
    }

}
