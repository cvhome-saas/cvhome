package com.asrevo.cvhome.store.model.references;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeSerializer;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeSerializer;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseAddress implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ZoneCodeSerializer.class)
    @JsonDeserialize(using = ZoneCodeDeSerializer.class)
    private ZoneCode stateProvince;

    @JsonSerialize(using = CountryIsoCodeSerializer.class)
    @JsonDeserialize(using = CountryIsoCodeDeSerializer.class)
    private CountryIsoCode country;

    private String address;

    private String postalCode;

    private String city;

    private boolean active = true;

}
