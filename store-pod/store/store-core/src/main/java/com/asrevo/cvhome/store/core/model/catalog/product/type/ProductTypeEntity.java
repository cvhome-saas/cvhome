package com.asrevo.cvhome.store.core.model.catalog.product.type;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

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
