package com.asrevo.cvhome.store.core.model.shoppingcart;

import com.asrevo.cvhome.store.core.model.entity.ShopEntity;
import com.asrevo.cvhome.store.core.model.order.total.OrderTotal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;


@Setter
@Getter
@Component
@Scope(value = "prototype")
public class ShoppingCartData extends ShopEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String message;
    private String code;
    private int quantity;
    private String total;
    private String subTotal;
    private Long orderId;

    private List<OrderTotal> totals;//calculated from OrderTotalSummary
    private List<ShoppingCartItem> shoppingCartItems;
    private List<ShoppingCartItem> unavailables;


}
