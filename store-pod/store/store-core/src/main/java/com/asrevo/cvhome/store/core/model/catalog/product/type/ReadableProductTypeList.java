package com.asrevo.cvhome.store.core.model.catalog.product.type;

import com.asrevo.cvhome.store.core.model.entity.ReadableList;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ReadableProductTypeList extends ReadableList {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    List<ReadableProductType> list = new ArrayList<ReadableProductType>();

    public void setList(List<ReadableProductType> list) {
        this.list = list;
    }

}
