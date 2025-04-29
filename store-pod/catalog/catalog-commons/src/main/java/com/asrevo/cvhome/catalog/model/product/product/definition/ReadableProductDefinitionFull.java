package com.asrevo.cvhome.catalog.model.product.product.definition;

import com.asrevo.cvhome.catalog.model.product.ProductDescription;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductDefinitionFull extends ReadableProductDefinition {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ProductDescription> descriptions = new ArrayList<>();
}
