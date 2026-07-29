package com.asrevo.cvhome.store.core.entity.order.orderstatus;

import lombok.Getter;

@Getter
public enum OrderStatus {

    CREATED("created"),  PENDING_PAYMENT("pending_payment"), CONFIRMED("confirmed"),
    PROCESSING("processing"), SHIPPED("shipped"),
    DELIVERING("delivering"), DELIVERED("delivered"), COMPLETED("completed"),
    CANCELLED("cancelled"), RETURNED("returned");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

}