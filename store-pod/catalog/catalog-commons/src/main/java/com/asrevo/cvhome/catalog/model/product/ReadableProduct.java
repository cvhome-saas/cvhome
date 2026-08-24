package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductAttribute;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductOption;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductProperty;
import com.asrevo.cvhome.catalog.model.product.product.ProductEntity;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProduct extends ProductEntity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private ProductDescription description;

    private boolean discounted = false;

    private ReadableImage image;

    private List<ReadableImage> images = new ArrayList<>();

    private ReadableManufacturer manufacturer;

    private List<ReadableProductAttribute> attributes = new ArrayList<>();

    private List<ReadableProductOption> options = new ArrayList<>();

    private List<ReadableProductProperty> properties = new ArrayList<>();

    private List<ReadableCategory> categories = new ArrayList<>();

    private ReadableProductType type;

    private boolean canBePurchased = false;

}
