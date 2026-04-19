package com.asrevo.cvhome.store.core.entity.order.payment;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.asrevo.cvhome.store.core.entity.payments.CreditCardType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Embeddable
public class CreditCard implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "CARD_TYPE")
    @Enumerated(value = EnumType.STRING)
    private CreditCardType cardType;

    @Column(name = "CC_OWNER")
    private String ccOwner;

    @Column(name = "CC_NUMBER")
    private String ccNumber;

    @Column(name = "CC_EXPIRES")
    private String ccExpires;

    @Column(name = "CC_CVV")
    private String ccCvv;

}
