package com.asrevo.cvhome.catalog.model.product.attribute.api;

import com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionEntity;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductOptionEntity extends ProductOptionEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ProductOptionDescription description;
}
