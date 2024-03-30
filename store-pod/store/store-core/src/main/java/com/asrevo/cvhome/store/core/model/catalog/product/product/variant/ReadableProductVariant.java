package com.asrevo.cvhome.store.core.model.catalog.product.product.variant;

import com.asrevo.cvhome.store.core.model.catalog.product.inventory.ReadableInventory;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import com.asrevo.cvhome.store.core.model.catalog.product.variation.ReadableProductVariation;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProductVariant extends ProductVariant {

    private static final long serialVersionUID = 1L;

    private ReadableProductVariation variation;
    private ReadableProductVariation variationValue;
    private String code;
    private List<ReadableImage> images = new ArrayList<ReadableImage>();
    private List<ReadableInventory> inventory = new ArrayList<ReadableInventory>();

}
