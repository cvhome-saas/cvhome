package com.asrevo.cvhome.store.core.entity.payments;

import lombok.Getter;

@Getter
public enum PaymentType {

    COD("COD"), PAYPAL("PAYPAL"), STRIPE("STRIPE");

    private final String type;

    PaymentType(String type) {
        this.type = type;
    }

}
