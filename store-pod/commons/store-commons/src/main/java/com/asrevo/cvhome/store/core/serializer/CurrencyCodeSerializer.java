package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class CurrencyCodeSerializer extends StdSerializer<CurrencyCode> {

    public CurrencyCodeSerializer() {
        this(null);
    }

    protected CurrencyCodeSerializer(Class<CurrencyCode> t) {
        super(t);
    }

    @Override
    public void serialize(CurrencyCode value, JsonGenerator gen, SerializationContext provider)
            throws JacksonException {
        gen.writeString(value.code());
    }

}
