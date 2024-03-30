package com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup;

import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProductVariantGroup extends ProductVariantGroup {

    private static final long serialVersionUID = 1L;

    List<ReadableImage> images = new ArrayList<ReadableImage>();

    private List<ReadableProductVariant> productVariants = new ArrayList<ReadableProductVariant>();

}
