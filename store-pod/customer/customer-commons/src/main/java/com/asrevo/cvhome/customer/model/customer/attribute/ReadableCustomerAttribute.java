package com.asrevo.cvhome.customer.model.customer.attribute;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableCustomerAttribute extends CustomerAttributeEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ReadableCustomerOption customerOption;
    private ReadableCustomerOptionValue customerOptionValue;
}
