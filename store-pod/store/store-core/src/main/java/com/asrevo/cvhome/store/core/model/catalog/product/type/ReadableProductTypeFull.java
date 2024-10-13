package com.asrevo.cvhome.store.core.model.catalog.product.type;

import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductTypeFull extends ReadableProductType {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ProductTypeDescription> descriptions;
}
