package com.asrevo.cvhome.store.core.model.order;

import com.asrevo.cvhome.store.core.entity.order.OrderTotal;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Output object after total calculation
 *
 * @author Carl Samson
 */
@Setter
@Getter
public class OrderTotalSummary implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private BigDecimal subTotal; // one time price for items
    private BigDecimal total; // final price
    private BigDecimal taxTotal; // total of taxes

    private List<OrderTotal> totals; // all other fees (tax, shipping ....)
}
