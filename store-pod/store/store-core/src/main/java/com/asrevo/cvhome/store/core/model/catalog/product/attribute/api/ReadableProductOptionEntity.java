package com.asrevo.cvhome.store.core.model.catalog.product.attribute.api;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableProductOptionEntity extends ProductOptionEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private ProductOptionDescription description;

}
