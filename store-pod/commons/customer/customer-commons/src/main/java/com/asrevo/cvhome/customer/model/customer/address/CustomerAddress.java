package com.asrevo.cvhome.customer.model.customer.address;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.ZoneCode;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeSerializer;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeSerializer;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

/**
 * Customer or someone address
 *
 * @author carlsamson
 */
@Setter
@Getter
public class CustomerAddress extends AddressLocation implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String firstName;

    private String lastName;

    private String company;

    private String phone;

    private String address;

    private String city;

    private String stateProvince;

    @JsonSerialize(using = ZoneCodeSerializer.class)
    @JsonDeserialize(using = ZoneCodeDeSerializer.class)
    private ZoneCode zone; // code

    @JsonSerialize(using = CountryIsoCodeSerializer.class)
    @JsonDeserialize(using = CountryIsoCodeDeSerializer.class)
    private CountryIsoCode country;

}
