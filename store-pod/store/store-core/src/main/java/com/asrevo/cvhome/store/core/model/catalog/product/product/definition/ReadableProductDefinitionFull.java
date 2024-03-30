package com.asrevo.cvhome.store.core.model.catalog.product.product.definition;

import com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProductDefinitionFull extends ReadableProductDefinition {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<ProductDescription> descriptions = new ArrayList<ProductDescription>();

}
