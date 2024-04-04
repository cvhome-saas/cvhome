package com.asrevo.cvhome.store.core.model.catalog.product.product.definition;

import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.store.core.model.catalog.product.inventory.ReadableInventory;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProductDefinition extends ProductDefinition {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private ReadableProductType type;
    private List<ReadableCategory> categories = new ArrayList<ReadableCategory>();
    private ReadableManufacturer manufacturer;
    private ProductDescription description = null;
    private List<PersistableProductAttribute> properties = new ArrayList<PersistableProductAttribute>();
    private List<ReadableImage> images = new ArrayList<ReadableImage>();
    private ReadableInventory inventory;


}
