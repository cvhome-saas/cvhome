package com.asrevo.cvhome.store.core.model.catalog.product.attribute.api;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableProductAttributeEntity extends ProductAttributeEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String productAttributeWeight;
    private String productAttributePrice;
    private String productAttributeUnformattedPrice;

    private ReadableProductOptionEntity option;
    private ReadableProductOptionValue optionValue;


}
