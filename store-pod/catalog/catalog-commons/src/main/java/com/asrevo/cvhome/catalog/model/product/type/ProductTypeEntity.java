package com.asrevo.cvhome.catalog.model.product.type;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductTypeEntity extends Entity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    boolean allowAddToCart;

    private String code;

    private boolean visible;

}
