package com.asrevo.cvhome.catalog.model.product.attribute;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

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
