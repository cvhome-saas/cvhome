package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

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
