package com.asrevo.cvhome.store.core.model.catalog.product.attribute.optionset;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class ProductOptionSetEntity implements Serializable {


    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String code;
    private boolean readOnly;

}
