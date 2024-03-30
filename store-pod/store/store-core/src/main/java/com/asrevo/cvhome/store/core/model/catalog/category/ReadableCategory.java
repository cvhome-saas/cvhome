package com.asrevo.cvhome.store.core.model.catalog.category;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableCategory extends CategoryEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private CategoryDescription description;//one category based on language
    private int productCount;
    private String store;
    private List<ReadableCategory> children = new ArrayList<ReadableCategory>();

}
