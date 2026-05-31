package com.asrevo.cvhome.store.core.entity.order.orderstatus;

import lombok.Getter;

@Getter
public enum OrderStatus {

    ORDERED("ordered"), PROCESSED("processed"), DELIVERED("delivered"), REFUNDED("refunded"), CANCELED("canceled"),
    PENDING_PAYMENT("pending_payment");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

}