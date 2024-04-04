package com.asrevo.cvhome.store.core.model.payments;

import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import lombok.Getter;
import lombok.Setter;

/**
 * When the user performs a payment using paypal
 *
 * @author Carl Samson
 */
@Setter
@Getter
public class PaypalPayment extends Payment {

    //express checkout
    private String payerId;
    private String paymentToken;

    public PaypalPayment() {
        super.setPaymentType(PaymentType.PAYPAL);
    }

}
