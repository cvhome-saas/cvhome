package com.asrevo.cvhome.store.core.model.catalog.product;

import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductAttribute;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductOption;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductProperty;
import com.asrevo.cvhome.store.core.model.catalog.product.product.ProductEntity;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProduct extends ProductEntity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private ProductDescription description;
    private ReadableProductPrice productPrice;
    private String finalPrice = "0";
    private String originalPrice = null;
    private boolean discounted = false;
    private ReadableImage image;
    private List<ReadableImage> images = new ArrayList<>();
    private ReadableManufacturer manufacturer;
    private List<ReadableProductAttribute> attributes = new ArrayList<>();
    private List<ReadableProductOption> options = new ArrayList<>();
    private List<ReadableProductVariant> variants = new ArrayList<>();
    private List<ReadableProductProperty> properties = new ArrayList<>();
    private List<ReadableCategory> categories = new ArrayList<>();
    private ReadableProductType type;
    private boolean canBePurchased = false;

    // RENTAL
    private RentalOwner owner;


}
