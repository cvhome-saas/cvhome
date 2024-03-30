package com.asrevo.cvhome.store.core.model.customer.attribute;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableCustomerAttribute extends CustomerAttributeEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ReadableCustomerOption customerOption;
    private ReadableCustomerOptionValue customerOptionValue;


}
