package com.asrevo.cvhome.store.core.model.shoppingcart;

import com.asrevo.cvhome.store.core.model.catalog.product.ReadableMinimalProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.variation.ReadableProductVariation;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private static final long serialVersionUID = 1L;
    private BigDecimal subTotal;
    private String displaySubTotal;
    private List<ReadableShoppingCartAttribute> cartItemattributes = new ArrayList<ReadableShoppingCartAttribute>();

    private ReadableProductVariation variant = null;
    private ReadableProductVariation variantValue = null;


}
