package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * What the payment-return page reads to decide what to show. Twelve themes read these four fields by name.
 */
@Getter
@Setter
public class ReadableOrderStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    private String redirectUrl;
}
