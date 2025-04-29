package com.asrevo.cvhome.store.core.model.catalog;

import com.asrevo.cvhome.store.core.model.entity.ShopEntity;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class NamedEntity extends ShopEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private String friendlyUrl;
    private String keyWords;
    private String highlights;
    private String metaDescription;
    private String title;
}
