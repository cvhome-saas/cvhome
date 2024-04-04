package com.asrevo.cvhome.store.core.model.catalog.catalog;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class CatalogEntity extends Entity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean visible;
    private boolean defaultCatalog;
    private String code;

}
