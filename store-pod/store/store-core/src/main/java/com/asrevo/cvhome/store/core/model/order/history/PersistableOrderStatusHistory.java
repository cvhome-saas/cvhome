package com.asrevo.cvhome.store.core.model.order.history;

import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatusHistory;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableOrderStatusHistory extends OrderStatusHistory {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String date;
}
