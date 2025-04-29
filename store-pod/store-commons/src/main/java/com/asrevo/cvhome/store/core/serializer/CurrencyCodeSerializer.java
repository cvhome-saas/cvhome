package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class CurrencyCodeSerializer extends StdSerializer<CurrencyCode> {

    public CurrencyCodeSerializer() {
        this(null);
    }

    protected CurrencyCodeSerializer(Class<CurrencyCode> t) {
        super(t);
    }

    @Override
    public void serialize(CurrencyCode value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeString(value.code());
    }
}
