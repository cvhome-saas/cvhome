package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Input object used when selecting an item option
 *
 * @author carlsamson
 */
@Setter
@Getter
public class ReadableSelectedProductVariant implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<ReadableProductVariantValue> options = new ArrayList<ReadableProductVariantValue>();

}
