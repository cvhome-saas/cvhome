package com.asrevo.cvhome.store.core.model.order.v1;

import com.asrevo.cvhome.store.core.model.order.transaction.PersistablePayment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * This object is used when processing an order from the API
 * It will be used for processing the payment and as Order meta data
 *
 * @author c.samson
 */
@Setter
@Getter
public class PersistableOrder extends Order {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private PersistablePayment payment;
    private Long shippingQuote;
    @JsonIgnore
    private Long shoppingCartId;
    @JsonIgnore
    private Long customerId;


}
