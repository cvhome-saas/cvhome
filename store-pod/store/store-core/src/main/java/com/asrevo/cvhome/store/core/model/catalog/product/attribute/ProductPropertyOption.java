package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import com.asrevo.cvhome.commons.domain.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;


@Setter
@Getter
public class ProductPropertyOption extends Entity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String code;
    private String type;
    private boolean readOnly;

}