package com.asrevo.cvhome.store.core.model.customer.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class CustomerOptionValueEntity extends CustomerOptionValue implements
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int order;
    private String code;

}
