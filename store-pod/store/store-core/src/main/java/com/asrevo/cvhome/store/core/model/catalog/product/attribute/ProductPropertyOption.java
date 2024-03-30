package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Setter
@Getter
public class ProductPropertyOption extends Entity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String code;
    private String type;
    private boolean readOnly;

}