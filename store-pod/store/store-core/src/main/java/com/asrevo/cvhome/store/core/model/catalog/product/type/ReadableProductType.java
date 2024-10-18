package com.asrevo.cvhome.store.core.model.catalog.product.type;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductType extends ProductTypeEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ProductTypeDescription description;
}
