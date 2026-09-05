package com.asrevo.cvhome.checkout.model.order.v1;

import java.io.Serial;

import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;

import lombok.Getter;
import lombok.Setter;

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
