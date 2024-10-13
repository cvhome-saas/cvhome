package com.asrevo.cvhome.store.core.model.entity;

import com.asrevo.cvhome.commons.domain.Entity;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class ShopEntity extends Entity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String language;
}
