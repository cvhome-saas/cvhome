package com.asrevo.cvhome.store.core.model.catalog.product.attribute.optionset;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductOption;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductOptionValue;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductType;
import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductOptionSet extends ProductOptionSetEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ReadableProductOption option;
    private List<ReadableProductOptionValue> values;
    private List<ReadableProductType> productTypes;
}
