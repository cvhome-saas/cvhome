package com.asrevo.cvhome.catalog.model.product.product.variant;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.catalog.model.product.ReadableImage;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductVariantGroup extends ProductVariantGroup {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<ReadableImage> images = new ArrayList<>();

    private List<ReadableProductVariant> productVariants = new ArrayList<>();

}
