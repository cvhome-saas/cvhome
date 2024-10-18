package com.asrevo.cvhome.store.core.model.catalog.product.attribute.api;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionEntity;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProductOptionEntity extends ProductOptionEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ProductOptionDescription> descriptions = new ArrayList<>();
}
