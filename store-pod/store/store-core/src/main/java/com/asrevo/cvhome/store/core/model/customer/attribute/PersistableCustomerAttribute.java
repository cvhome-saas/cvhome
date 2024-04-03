package com.asrevo.cvhome.store.core.model.customer.attribute;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableCustomerAttribute extends CustomerAttributeEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private CustomerOption customerOption;
    private CustomerOptionValue customerOptionValue;


}
