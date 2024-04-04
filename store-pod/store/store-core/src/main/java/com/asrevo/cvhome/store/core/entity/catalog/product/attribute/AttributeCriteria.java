package com.asrevo.cvhome.store.core.entity.catalog.product.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class AttributeCriteria implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String attributeCode;
    private String attributeValue;

}
