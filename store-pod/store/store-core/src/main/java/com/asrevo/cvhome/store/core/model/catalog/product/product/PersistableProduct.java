package com.asrevo.cvhome.store.core.model.catalog.product.product;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.store.core.model.catalog.category.Category;
import com.asrevo.cvhome.store.core.model.catalog.product.PersistableImage;
import com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class PersistableProduct extends ProductEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private List<ProductDescription> descriptions = new ArrayList<ProductDescription>();
    private List<PersistableProductAttribute> attributes = new ArrayList<PersistableProductAttribute>();//persist attribute and save reference
    private List<PersistableImage> images;//persist images and save reference
    private List<Category> categories = new ArrayList<Category>();
    private PersistableProductInventory inventory;
    private List<PersistableProductVariant> variants = new ArrayList<PersistableProductVariant>();
    private String type;

}
