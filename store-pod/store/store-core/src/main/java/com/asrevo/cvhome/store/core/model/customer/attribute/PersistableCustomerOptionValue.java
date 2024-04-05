package com.asrevo.cvhome.store.core.model.customer.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class PersistableCustomerOptionValue extends CustomerOptionValueEntity
        implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private List<CustomerOptionValueDescription> descriptions;

}
