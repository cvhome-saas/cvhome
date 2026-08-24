package com.asrevo.cvhome.inventory.model.price;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductPriceFull extends ReadableProductPrice {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<ProductPriceDescription> descriptions = new ArrayList<>();

}
