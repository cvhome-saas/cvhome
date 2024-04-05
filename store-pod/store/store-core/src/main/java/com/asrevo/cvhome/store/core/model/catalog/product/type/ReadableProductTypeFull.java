package com.asrevo.cvhome.store.core.model.catalog.product.type;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Setter
@Getter
public class ReadableProductTypeFull extends ReadableProductType {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ProductTypeDescription> descriptions;

}
