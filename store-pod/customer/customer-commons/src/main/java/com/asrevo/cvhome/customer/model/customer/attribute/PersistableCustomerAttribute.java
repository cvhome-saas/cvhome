package com.asrevo.cvhome.customer.model.customer.attribute;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableCustomerAttribute extends CustomerAttributeEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private CustomerOption customerOption;
    private CustomerOptionValue customerOptionValue;
}
