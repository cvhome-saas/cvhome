package com.asrevo.cvhome.store.core.model.customer.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ReadableCustomerOption extends CustomerOptionEntity
        implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private CustomerOptionDescription description;


}
