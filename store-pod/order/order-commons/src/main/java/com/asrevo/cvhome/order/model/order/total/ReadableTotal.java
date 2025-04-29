package com.asrevo.cvhome.order.model.order.total;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Serves as the order total summary calculation
 *
 * @author c.samson
 */
@Setter
@Getter
public class ReadableTotal implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ReadableOrderTotal> totals;
    private String grandTotal;
}
