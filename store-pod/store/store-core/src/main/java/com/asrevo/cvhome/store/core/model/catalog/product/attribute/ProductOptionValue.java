package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;


@Setter
@Getter
public class ProductOptionValue extends Entity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String code;
    private String name;
    private boolean defaultValue;
    private int sortOrder;
    private String image;


}
