package com.asrevo.cvhome.checkout.model.shoppingcart;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;

import lombok.Getter;
import lombok.Setter;

/**
 * compatible with v1 version
 *
 * @author c.samson
 */
@Setter
@Getter
public class ReadableShoppingCartItem extends ReadableMinimalProduct implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The line's unit price as checkout recorded it — no longer inherited from the catalog product DTO, which since
     * the catalog/inventory split carries no price or quantity.
     */
    private BigDecimal price;

    private String finalPrice;

    private int quantity;

    private BigDecimal subTotal;

    private String displaySubTotal;

}
