package com.asrevo.cvhome.store.core.model.catalog.category;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class CategoryEntity extends Category implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    private int sortOrder;
    private boolean visible;
    private boolean featured;
    private String lineage;
    private int depth;
    private Category parent;


}
