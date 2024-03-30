package com.asrevo.cvhome.store.core.model.customer.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class CustomerOptionEntity extends CustomerOption implements
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int order;
    private String code;
    private String type;//TEXT|SELECT|RADIO|CHECKBOX

}
