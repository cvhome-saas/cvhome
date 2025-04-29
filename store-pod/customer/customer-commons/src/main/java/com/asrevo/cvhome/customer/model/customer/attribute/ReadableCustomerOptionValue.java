package com.asrevo.cvhome.customer.model.customer.attribute;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableCustomerOptionValue extends CustomerOptionValueEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private CustomerOptionValueDescription description;
}
