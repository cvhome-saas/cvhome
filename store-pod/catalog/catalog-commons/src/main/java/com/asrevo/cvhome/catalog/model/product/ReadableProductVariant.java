package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * One sellable combination as the storefront reads it. Price and stock come from the inventory availability
 * call, keyed by {@code sku}; the ids in {@code optionValueIds} are store-wide, matching the product's
 * {@code options[]}.
 */
@Getter
@Setter
public class ReadableProductVariant extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sku;

    private Integer sortOrder;

    private boolean defaultVariant;

    private List<Long> optionValueIds = new ArrayList<>();
}
