package com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup;

import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.ReadableProductVariant;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductVariantGroup extends ProductVariantGroup {

    @Serial private static final long serialVersionUID = 1L;

    List<ReadableImage> images = new ArrayList<>();

    private List<ReadableProductVariant> productVariants = new ArrayList<>();
}
