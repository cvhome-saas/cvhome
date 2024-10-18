package com.asrevo.cvhome.store.core.model.catalog.catalog;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableCatalogCategoryEntry extends CatalogEntryEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String productCode;
    private String categoryCode;
}
