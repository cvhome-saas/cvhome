package com.asrevo.cvhome.store.core.entity.payments;

import lombok.Getter;

@Getter
public enum PaymentType {

    CREDITCARD("creditcard"), FREE("free"), COD("cod"), MONEYORDER("moneyorder"), PAYPAL("paypal"), INVOICE("invoice"),
    DIRECTBANK("directbank"), PAYMENTPLAN("paymentplan"), ACCOUNTCREDIT("accountcredit");

    private final String type;

    PaymentType(String type) {
        this.type = type;
    }

}
