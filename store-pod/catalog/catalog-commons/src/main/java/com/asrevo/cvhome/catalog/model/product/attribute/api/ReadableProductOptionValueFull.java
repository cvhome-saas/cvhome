package com.asrevo.cvhome.catalog.model.product.attribute.api;

import com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValueDescription;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductOptionValueFull extends ReadableProductOptionValue {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ProductOptionValueDescription> descriptions = new ArrayList<>();
}
