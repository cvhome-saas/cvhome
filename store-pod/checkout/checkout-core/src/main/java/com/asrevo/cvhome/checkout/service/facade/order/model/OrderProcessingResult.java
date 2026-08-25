package com.asrevo.cvhome.checkout.service.facade.order.model;

import com.asrevo.cvhome.checkout.entity.order.Order;

public record OrderProcessingResult(Order order, String redirectUrl) {
    public OrderProcessingResult(Order order) {
        this(order, null);
    }

    public boolean redirectRequired() {
        return redirectUrl != null;
    }
}