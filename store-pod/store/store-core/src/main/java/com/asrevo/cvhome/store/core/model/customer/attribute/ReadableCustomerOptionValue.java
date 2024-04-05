package com.asrevo.cvhome.store.core.model.customer.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class ReadableCustomerOptionValue extends CustomerOptionValueEntity
        implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private CustomerOptionValueDescription description;


}
