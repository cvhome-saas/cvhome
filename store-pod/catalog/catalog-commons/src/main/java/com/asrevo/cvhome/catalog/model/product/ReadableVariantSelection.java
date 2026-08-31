package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * The variant a sku-addressed read resolved to: its sku and the option/value labels of the combination. Absent
 * on a default variant (there is nothing selected) and everywhere a read is not addressed by sku.
 */
@Getter
@Setter
public class ReadableVariantSelection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sku;

    private List<ReadableVariantOptionValue> optionValues = new ArrayList<>();
}
