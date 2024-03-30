package com.asrevo.cvhome.store.core.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public abstract class ShopEntity extends Entity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String language;


}
