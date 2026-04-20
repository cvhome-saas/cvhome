package com.asrevo.cvhome.checkout.model.shoppingcart;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import com.asrevo.cvhome.store.core.model.entity.ShopEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShoppingCartItem extends ShopEntity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    private String price;

    private String image;

    private BigDecimal productPrice;

    private int quantity;

    private String sku; // sku

    private String code; // shopping cart code

    private boolean productVirtual;

    private String subTotal;

    public int getQuantity() {
        if (quantity <= 0) {
            quantity = 1;
        }
        return quantity;
    }

}
