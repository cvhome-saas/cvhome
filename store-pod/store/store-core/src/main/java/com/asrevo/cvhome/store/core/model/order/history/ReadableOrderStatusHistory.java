package com.asrevo.cvhome.store.core.model.order.history;


import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableOrderStatusHistory extends OrderStatusHistory {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * YYYY-mm-DD:HH mm SSS
     */
    private String date;

}
