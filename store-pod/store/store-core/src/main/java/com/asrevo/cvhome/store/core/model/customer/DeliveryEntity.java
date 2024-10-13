package com.asrevo.cvhome.store.core.model.customer;

import com.asrevo.cvhome.store.core.model.customer.address.Address;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeliveryEntity extends Address implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String countryName;

    private String provinceName;
}
