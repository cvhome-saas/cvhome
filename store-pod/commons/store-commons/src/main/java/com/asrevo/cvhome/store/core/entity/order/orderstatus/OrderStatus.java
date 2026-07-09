package com.asrevo.cvhome.store.core.entity.order.orderstatus;

import lombok.Getter;

@Getter
public enum OrderStatus {

    CREATED("created"), PROCESSING("processing"), SHIPPED("shipped"), DELIVERED("delivered"), COMPLETED("completed"),
    CANCELLED("cancelled"), RETURNED("returned");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

}