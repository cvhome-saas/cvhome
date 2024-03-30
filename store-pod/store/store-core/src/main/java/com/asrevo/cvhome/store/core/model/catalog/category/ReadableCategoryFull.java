package com.asrevo.cvhome.store.core.model.catalog.category;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableCategoryFull extends ReadableCategory {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private List<CategoryDescription> descriptions = new ArrayList<CategoryDescription>();

}
