package com.asrevo.cvhome.store.core.model.shoppingcart;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductOptionValue;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableShoppingCartAttributeOptionValue extends ReadableProductOptionValue {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;

}
