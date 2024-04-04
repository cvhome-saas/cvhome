package com.asrevo.cvhome.store.core.model.order.v1;


import com.asrevo.cvhome.store.core.model.customer.PersistableCustomer;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableAnonymousOrder extends PersistableOrder {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private PersistableCustomer customer;

}
