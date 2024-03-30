package com.asrevo.cvhome.store.core.model.catalog.category;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PersistableCategory extends CategoryEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<CategoryDescription> descriptions;//always persist description
    private List<PersistableCategory> children = new ArrayList<PersistableCategory>();

}
