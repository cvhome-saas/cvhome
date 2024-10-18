package com.asrevo.cvhome.store.core.model.shoppingcart;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductAttribute;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Compatible with v1
 *
 * @author c.samson
 */
@Setter
@Getter
public class PersistableShoppingCartItem implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String product; // or product sku (instance or product)
    private int quantity;
    private String promoCode;
    private List<ProductAttribute> attributes;
}
