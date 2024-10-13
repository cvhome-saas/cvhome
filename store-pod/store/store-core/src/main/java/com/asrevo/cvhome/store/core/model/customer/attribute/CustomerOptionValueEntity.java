package com.asrevo.cvhome.store.core.model.customer.attribute;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerOptionValueEntity extends CustomerOptionValue implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private int order;
    private String code;
}
