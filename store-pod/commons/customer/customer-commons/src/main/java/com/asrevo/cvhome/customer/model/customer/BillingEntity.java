package com.asrevo.cvhome.customer.model.customer;

import java.io.Serial;

import com.asrevo.cvhome.customer.model.customer.address.Address;

import lombok.Getter;
import lombok.Setter;

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
