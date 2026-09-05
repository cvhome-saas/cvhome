package com.asrevo.cvhome.checkout.model.order.v1;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableOrderStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    private String redirectUrl;

}
