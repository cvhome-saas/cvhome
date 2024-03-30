package com.asrevo.cvhome.store.core.model.catalog.product;

import com.asrevo.cvhome.store.core.model.entity.ReadableList;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProductList extends ReadableList {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private List<ReadableProduct> products = new ArrayList<ReadableProduct>();

}
