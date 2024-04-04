package com.asrevo.cvhome.store.core.model.order.transaction;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class PaymentEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String paymentModule;//stripe|paypal|braintree|moneyorder ...
    private String amount;

}
