package com.asrevo.cvhome.catalog.model.catalog;

import com.asrevo.cvhome.commons.domain.Entity;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CatalogEntity extends Entity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private boolean visible;
    private boolean defaultCatalog;
    private String code;
}
