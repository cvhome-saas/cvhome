package com.asrevo.cvhome.store.core.model.catalog.product.product.variant;

import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import com.asrevo.cvhome.store.core.model.catalog.product.inventory.ReadableInventory;
import com.asrevo.cvhome.store.core.model.catalog.product.variation.ReadableProductVariation;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductVariant extends ProductVariant {

    @Serial private static final long serialVersionUID = 1L;

    private ReadableProductVariation variation;
    private ReadableProductVariation variationValue;
    private String code;
    private List<ReadableImage> images = new ArrayList<>();
    private List<ReadableInventory> inventory = new ArrayList<>();
}
