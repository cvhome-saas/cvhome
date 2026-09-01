package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * The console's view of one variant: the storefront shape plus the resolved option/value labels the matrix
 * renders in its read-only cells.
 */
@Getter
@Setter
public class ReadableProductVariantDefinition extends ReadableProductVariant {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<ReadableVariantOptionValue> optionValues = new ArrayList<>();
}
