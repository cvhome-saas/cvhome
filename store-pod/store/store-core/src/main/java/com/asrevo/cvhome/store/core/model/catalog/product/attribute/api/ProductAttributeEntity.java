package com.asrevo.cvhome.store.core.model.catalog.product.attribute.api;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductAttribute;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductAttributeEntity extends ProductAttribute implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private int sortOrder;
    private boolean attributeDefault = false;
    private boolean attributeDisplayOnly = false;
}
