package com.asrevo.cvhome.store.core.entity.order.orderstatus;

import lombok.Getter;

@Getter
public enum OrderStatus {
    CREATED, PENDING_PAYMENT, CONFIRMED, PROCESSING, SHIPPED, DELIVERING, DELIVERED, COMPLETED, CANCELLED, RETURNED;
}