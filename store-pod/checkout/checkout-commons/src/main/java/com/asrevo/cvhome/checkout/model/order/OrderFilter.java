package com.asrevo.cvhome.checkout.model.order;

import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

/**
 * The console's order list filters. Every field optional; the store is never a filter but always applied.
 */
public record OrderFilter(String name, Long id, OrderStatus status, String phone, String email, Long customerId) {

    public static OrderFilter none() {
        return new OrderFilter(null, null, null, null, null, null);
    }
}
