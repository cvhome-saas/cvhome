package com.asrevo.cvhome.store.core.model.customer;


import com.asrevo.cvhome.store.core.model.customer.address.Address;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;


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
