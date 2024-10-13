package com.asrevo.cvhome.store.core.model.customer.attribute;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableCustomerOptionValue extends CustomerOptionValueEntity
        implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<CustomerOptionValueDescription> descriptions;
}
