package com.asrevo.cvhome.store.core.model.catalog.product;

import com.asrevo.cvhome.commons.domain.ReadableList;
import com.asrevo.cvhome.store.core.model.catalog.product.group.ProductGroup;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductList extends ReadableList {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ProductGroup productGroup;

    private List<ReadableProduct> products = new ArrayList<>();
}
