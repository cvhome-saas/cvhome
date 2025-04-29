package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class CountryIsoCodeSerializer extends StdSerializer<CountryIsoCode> {

    public CountryIsoCodeSerializer() {
        this(null);
    }

    protected CountryIsoCodeSerializer(Class<CountryIsoCode> t) {
        super(t);
    }

    @Override
    public void serialize(CountryIsoCode value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeString(value.isoCode());
    }
}
