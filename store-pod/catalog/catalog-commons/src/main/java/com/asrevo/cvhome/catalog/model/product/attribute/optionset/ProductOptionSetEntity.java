package com.asrevo.cvhome.catalog.model.product.attribute.optionset;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductOptionSetEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private boolean readOnly;
}
