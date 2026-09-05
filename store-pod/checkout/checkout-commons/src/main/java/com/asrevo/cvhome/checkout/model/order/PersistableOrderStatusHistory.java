package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.validation.constraints.NotNull;

import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * The console's one write on an order: move it to {@code orderStatus} with an optional comment.
 */
@Getter
@Setter
public class PersistableOrderStatusHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;

    @NotNull
    private OrderStatus orderStatus;

    private String comments;

    private Instant date;
}
