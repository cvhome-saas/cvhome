package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.api.ProductOptionValueEntity;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProductOptionValue extends ProductOptionValueEntity
        implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ProductOptionValueDescription> descriptions = new ArrayList<>();
}
