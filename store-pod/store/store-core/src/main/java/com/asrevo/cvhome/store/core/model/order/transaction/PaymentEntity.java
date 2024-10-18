package com.asrevo.cvhome.store.core.model.order.transaction;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String paymentModule; // stripe|paypal|braintree|moneyorder ...
    private String amount;
}
