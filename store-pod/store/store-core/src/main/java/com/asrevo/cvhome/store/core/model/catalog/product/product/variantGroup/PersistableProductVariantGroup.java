package com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PersistableProductVariantGroup extends ProductVariantGroup {

    private static final long serialVersionUID = 1L;

    List<Long> productVariants = null;

}
