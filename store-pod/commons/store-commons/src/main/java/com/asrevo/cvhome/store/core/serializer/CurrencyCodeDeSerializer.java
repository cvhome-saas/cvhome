package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class CurrencyCodeDeSerializer extends StdDeserializer<CurrencyCode> {

    public CurrencyCodeDeSerializer() {
        this(CurrencyCode.class);
    }

    protected CurrencyCodeDeSerializer(Class<CurrencyCode> t) {
        super(t);
    }

    @Override
    public CurrencyCode deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String code = p.getString();
        return new CurrencyCode(code);
    }

}
