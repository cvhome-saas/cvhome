package com.asrevo.cvhome.store.core.model.catalog.product.attribute.api;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductOptionEntity extends ProductOptionEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ProductOptionDescription description;

}
