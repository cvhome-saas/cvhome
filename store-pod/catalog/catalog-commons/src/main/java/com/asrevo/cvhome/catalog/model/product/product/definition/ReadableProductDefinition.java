package com.asrevo.cvhome.catalog.model.product.product.definition;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.product.ProductDescription;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.catalog.model.product.inventory.ReadableInventory;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductDefinition extends ProductDefinition {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private ReadableProductType type;

    private List<ReadableCategory> categories = new ArrayList<>();

    private ReadableManufacturer manufacturer;

    private ProductDescription description = null;

    private List<PersistableProductAttribute> properties = new ArrayList<>();

    private List<ReadableImage> images = new ArrayList<>();

    private ReadableInventory inventory;

    private List<ProductDescription> descriptions = new ArrayList<>();

}
