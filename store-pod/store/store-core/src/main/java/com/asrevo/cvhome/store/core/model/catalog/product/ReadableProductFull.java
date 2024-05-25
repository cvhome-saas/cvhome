package com.asrevo.cvhome.store.core.model.catalog.product;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProductFull extends ReadableProduct {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    List<ProductDescription> descriptions = new ArrayList<>();

}
