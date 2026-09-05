package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The customer on the order could neither be found nor created, so there is nobody to place it for.
 */
public class OrderCustomerUnresolvedException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected OrderCustomerUnresolvedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static OrderCustomerUnresolvedException of(String cartCode) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_CUSTOMER_UNRESOLVED, OrderCustomerUnresolvedException::new)
                .detail("No customer could be resolved for cart %s.", cartCode)
                .param("cartCode", cartCode)
                .build();
    }

}
