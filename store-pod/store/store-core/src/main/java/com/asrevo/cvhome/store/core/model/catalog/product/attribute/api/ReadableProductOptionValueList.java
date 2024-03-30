package com.asrevo.cvhome.store.core.model.catalog.product.attribute.api;

import com.asrevo.cvhome.store.core.model.entity.ReadableList;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProductOptionValueList extends ReadableList {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    List<ReadableProductOptionValue> optionValues = new ArrayList<ReadableProductOptionValue>();

}
