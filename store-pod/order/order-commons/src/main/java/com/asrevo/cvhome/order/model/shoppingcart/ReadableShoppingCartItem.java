package com.asrevo.cvhome.order.model.shoppingcart;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.variation.ReadableProductVariation;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
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
    @Serial private static final long serialVersionUID = 1L;

    private BigDecimal subTotal;
    private String displaySubTotal;

    private ReadableProductVariation variant = null;
    private ReadableProductVariation variantValue = null;
}
