package com.asrevo.cvhome.store.core.model.order;

import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.model.shipping.ShippingSummary;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * This object is used as input object for many services
 * such as order total calculation and tax calculation
 *
 * @author Carl Samson
 */
@Setter
@Getter
public class OrderSummary implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private OrderSummaryType orderSummaryType = OrderSummaryType.ORDERTOTAL;
    private ShippingSummary shippingSummary;
    private String promoCode;
    private List<ShoppingCartItem> products = new ArrayList<>();
}
