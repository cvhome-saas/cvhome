package com.asrevo.cvhome.customer.model.customer;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.customer.model.customer.address.Address;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeliveryEntity extends Address implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String countryName;

    private String provinceName;

}
