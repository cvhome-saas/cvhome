package com.asrevo.cvhome.store.core.model.order;

import com.asrevo.cvhome.store.core.entity.order.OrderTotal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains a list of negative OrderTotal variation
 * that will be shown in the order summary
 *
 * @author carlsamson
 */
@Setter
@Getter
public abstract class OrderTotalVariation {

    List<OrderTotal> variations = null;
}
