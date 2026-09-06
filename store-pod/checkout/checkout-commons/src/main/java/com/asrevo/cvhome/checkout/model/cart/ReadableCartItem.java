package com.asrevo.cvhome.checkout.model.cart;

import java.io.Serial;
import java.math.BigDecimal;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;

import lombok.Getter;
import lombok.Setter;

/**
 * A cart line: the catalog's minimal product (description, image, variant labels) plus the live price and quantity.
 * Extends the catalog shape on purpose — the themes read {@code description.name}, {@code image} and {@code sku}
 * straight off a line.
 */
@Getter
@Setter
public class ReadableCartItem extends ReadableMinimalProduct {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigDecimal price;

    private String finalPrice;

    private int quantity;

    private BigDecimal subTotal;

    private String displaySubTotal;

    private int quantityOrderMinimum;

    private int quantityOrderMaximum;
}
