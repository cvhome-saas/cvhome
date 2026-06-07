package com.asrevo.cvhome.store.core.entity.payments;

import lombok.Getter;

@Getter
public enum PaymentType {

    COD("COD"), MANUAL_TRANSFER("MANUAL_TRANSFER"), PAYPAL("PAYPAL"), STRIPE("STRIPE");

    private final String type;

    PaymentType(String type) {
        this.type = type;
    }

}
