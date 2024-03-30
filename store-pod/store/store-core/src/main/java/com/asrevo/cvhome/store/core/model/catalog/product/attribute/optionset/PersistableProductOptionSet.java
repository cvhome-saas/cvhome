package com.asrevo.cvhome.store.core.model.catalog.product.attribute.optionset;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PersistableProductOptionSet extends ProductOptionSetEntity {


    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<Long> optionValues;
    private List<Long> productTypes;
    private Long option;


}
