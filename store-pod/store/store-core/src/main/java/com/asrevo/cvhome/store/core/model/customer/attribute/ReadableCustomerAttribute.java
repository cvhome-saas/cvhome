package com.asrevo.cvhome.store.core.model.customer.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableCustomerAttribute extends CustomerAttributeEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private ReadableCustomerOption customerOption;
    private ReadableCustomerOptionValue customerOptionValue;


}
