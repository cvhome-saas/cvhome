package com.asrevo.cvhome.store.core.model.catalog.product.type;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableProductType extends ProductTypeEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private ProductTypeDescription description;

}
