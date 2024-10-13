package com.asrevo.cvhome.store.core.model.order.history;

import com.asrevo.cvhome.commons.domain.Entity;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderStatusHistory extends Entity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private long orderId;
    private String orderStatus;
    private String comments;
}
