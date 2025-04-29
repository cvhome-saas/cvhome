package com.asrevo.cvhome.order.model.shoppingcart;

import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductVariant;
import com.asrevo.cvhome.order.model.order.total.ReadableOrderTotal;
import java.io.Serial;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Compatible with v1 + v2
 *
 * @author c.samson
 */
@Setter
@Getter
public class ReadableShoppingCart extends ShoppingCartEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    List<ReadableShoppingCartItem> products = new ArrayList<>();
    List<ReadableOrderTotal> totals;
    private String code;
    private BigDecimal subtotal;
    private String displaySubTotal;
    private BigDecimal total;
    private String displayTotal;
    private int quantity;
    private Long order;
    private String promoCode;
    private ReadableProductVariant variant;
    private Long customer;
}
