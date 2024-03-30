package com.asrevo.cvhome.store.core.model.catalog.catalog;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CatalogEntryEntity extends Entity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String catalog;
    private boolean visible;

}
