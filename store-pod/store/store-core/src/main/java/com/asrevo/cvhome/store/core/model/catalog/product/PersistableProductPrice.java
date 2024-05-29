package com.asrevo.cvhome.store.core.model.catalog.product;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PersistableProductPrice extends ProductPriceEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String sku;
    private Long productAvailabilityId;

    private List<ProductPriceDescription> descriptions = new ArrayList<>();

}
