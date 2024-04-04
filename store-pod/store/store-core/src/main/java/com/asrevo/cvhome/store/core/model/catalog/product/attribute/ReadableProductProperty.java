package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableProductProperty extends ProductPropertyOption {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Property use option objects
     */
    private ReadableProductOption property = null;
    private ReadableProductPropertyValue propertyValue = null;


}
