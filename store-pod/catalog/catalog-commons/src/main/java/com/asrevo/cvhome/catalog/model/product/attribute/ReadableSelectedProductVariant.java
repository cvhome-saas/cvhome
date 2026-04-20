package com.asrevo.cvhome.catalog.model.product.attribute;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

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
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ReadableProductVariantValue> options = new ArrayList<>();

}
