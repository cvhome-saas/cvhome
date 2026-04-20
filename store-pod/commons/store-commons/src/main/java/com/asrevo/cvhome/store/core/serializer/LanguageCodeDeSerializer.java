package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class LanguageCodeDeSerializer extends StdDeserializer<LanguageCode> {

    public LanguageCodeDeSerializer() {
        this(LanguageCode.class);
    }

    protected LanguageCodeDeSerializer(Class<LanguageCode> t) {
        super(t);
    }

    @Override
    public LanguageCode deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String code = p.getString();
        return new LanguageCode(code);
    }

}
