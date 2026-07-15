package com.asrevo.cvhome.store.model.references;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.Entity;
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

@Setter
@Getter
public class ZoneEntity extends Entity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = CountryIsoCodeSerializer.class)
    @JsonDeserialize(using = CountryIsoCodeDeSerializer.class)
    private CountryIsoCode countryCode;

    @JsonSerialize(using = ZoneCodeSerializer.class)
    @JsonDeserialize(using = ZoneCodeDeSerializer.class)
    private ZoneCode code;

}
