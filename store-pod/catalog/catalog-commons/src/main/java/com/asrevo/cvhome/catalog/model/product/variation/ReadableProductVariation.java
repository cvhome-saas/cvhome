package com.asrevo.cvhome.catalog.model.product.variation;

import java.io.Serial;

import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductOption;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductOptionValue;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductVariation extends ProductVariationEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    ReadableProductOption option = null;

    ReadableProductOptionValue optionValue = null;

}
