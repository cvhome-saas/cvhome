package com.asrevo.cvhome.store.core.model.catalog.product.type;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PersistableProductType extends ProductTypeEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<ProductTypeDescription> descriptions;

}
