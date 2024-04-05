package com.asrevo.cvhome.store.core.entity.catalog.product;

import com.asrevo.cvhome.store.core.entity.common.EntityList;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ProductList extends EntityList {


    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 7267292601646149482L;
    private List<Product> products = new ArrayList<>();


}
