package com.asrevo.cvhome.catalog.model.product.product.variant;

import com.asrevo.cvhome.catalog.model.product.Product;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductVariant extends Product {

    @Serial private static final long serialVersionUID = 1L;
    private String store;

    /**
     * use product id or sku
     **/
    private Long productId;

    private String sku;

    /**
     *
     **/
    private boolean available;

    private String dateAvailable;
    private int sortOrder;

    private boolean defaultSelection;
}
