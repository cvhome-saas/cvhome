package com.asrevo.cvhome.checkout.model.order.history;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderStatusHistory extends Entity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private long orderId;

    private OrderStatus orderStatus;

    private String comments;

}
