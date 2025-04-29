package com.asrevo.cvhome.catalog.entity.product;

import com.asrevo.cvhome.store.core.entity.common.EntityList;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductList extends EntityList {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 7267292601646149482L;

    private List<Product> products = new ArrayList<>();
}
