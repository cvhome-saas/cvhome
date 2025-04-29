package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class CurrencyCodeDeSerializer extends StdDeserializer<CurrencyCode> {

    public CurrencyCodeDeSerializer() {
        this(null);
    }

    protected CurrencyCodeDeSerializer(Class<CurrencyCode> t) {
        super(t);
    }

    @Override
    public CurrencyCode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String code = p.getText();
        return new CurrencyCode(code);
    }
}
