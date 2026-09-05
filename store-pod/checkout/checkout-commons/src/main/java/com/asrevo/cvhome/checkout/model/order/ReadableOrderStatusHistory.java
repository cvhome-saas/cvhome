package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;
import java.time.Instant;

import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableOrderStatusHistory extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;

    private OrderStatus orderStatus;

    private String comments;

    private Instant date;
}
