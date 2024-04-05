package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableProductAttributeValue extends ProductOptionValue {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String lang;
    private String description;


}
