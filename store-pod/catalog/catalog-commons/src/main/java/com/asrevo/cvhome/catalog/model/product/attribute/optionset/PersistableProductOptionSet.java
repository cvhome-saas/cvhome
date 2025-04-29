package com.asrevo.cvhome.catalog.model.product.attribute.optionset;

import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProductOptionSet extends ProductOptionSetEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<Long> optionValues;
    private List<Long> productTypes;
    private Long option;
}
