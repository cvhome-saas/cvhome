package com.asrevo.cvhome.store.core.model.order.history;


import com.asrevo.cvhome.commons.domain.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class OrderStatusHistory extends Entity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private long orderId;
    private String orderStatus;
    private String comments;


}
