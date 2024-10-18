package com.asrevo.cvhome.store.core.model.catalog.catalog;

import com.asrevo.cvhome.commons.domain.Entity;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CatalogEntryEntity extends Entity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String catalog;
    private boolean visible;
}
