package com.asrevo.cvhome.store.core.model.catalog.product.variation;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductOption;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductOptionValue;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductVariation extends ProductVariationEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    ReadableProductOption option = null;
    ReadableProductOptionValue optionValue = null;

}
