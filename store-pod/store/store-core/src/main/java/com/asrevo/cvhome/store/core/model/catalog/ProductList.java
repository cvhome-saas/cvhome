package com.asrevo.cvhome.store.core.model.catalog;

import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductList implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private int productCount;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<ReadableProduct> products = new ArrayList<>();
}
