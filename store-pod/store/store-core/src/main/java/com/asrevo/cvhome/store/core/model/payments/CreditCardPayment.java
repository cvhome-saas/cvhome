package com.asrevo.cvhome.store.core.model.payments;

import com.asrevo.cvhome.store.core.entity.payments.CreditCardType;
import lombok.Getter;
import lombok.Setter;

/**
 * When the user performs a payment using a credit card
 *
 * @author Carl Samson
 */
@Setter
@Getter
public class CreditCardPayment extends Payment {

    private String creditCardNumber;
    private String credidCardValidationNumber;
    private String expirationMonth;
    private String expirationYear;
    private String cardOwner;
    private CreditCardType creditCard;

}
