package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.api.ReadableProductOptionValue;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductOption extends ProductPropertyOption {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String name;
    private String lang;
    private boolean variant;
    private List<ReadableProductOptionValue> optionValues = new ArrayList<>();
}
