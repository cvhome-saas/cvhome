package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.commons.domain.CountryIsoCode;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class CountryIsoCodeSerializer extends StdSerializer<CountryIsoCode> {

    public CountryIsoCodeSerializer() {
        this(null);
    }

    protected CountryIsoCodeSerializer(Class<CountryIsoCode> t) {
        super(t);
    }

    @Override
    public void serialize(CountryIsoCode value, JsonGenerator gen, SerializationContext provider)
            throws JacksonException {
        gen.writeString(value.isoCode());
    }

}
