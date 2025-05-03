package com.asrevo.cvhome.catalog.model.product;

import com.asrevo.cvhome.catalog.model.product.group.ProductGroup;
import com.asrevo.cvhome.commons.domain.ReadableList;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Deprecated
public class ReadableProductList extends ReadableList<ReadableProduct> {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ProductGroup productGroup;

    private List<ReadableProduct> content = new ArrayList<>();
}
