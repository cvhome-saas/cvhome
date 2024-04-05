package com.asrevo.cvhome.store.core.model.order.v1;


import com.asrevo.cvhome.store.core.model.customer.PersistableCustomer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class PersistableAnonymousOrder extends PersistableOrder {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private PersistableCustomer customer;

}
