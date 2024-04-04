package com.asrevo.cvhome.store.core.model.customer;


import com.asrevo.cvhome.store.core.model.customer.address.Address;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class BillingEntity extends Address {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String email;

    private String countryName;

    private String provinceName;

}
