package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class LanguageCodeDeSerializer extends StdDeserializer<LanguageCode> {

    public LanguageCodeDeSerializer() {
        this(null);
    }

    protected LanguageCodeDeSerializer(Class<LanguageCode> t) {
        super(t);
    }

    @Override
    public LanguageCode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String code = p.getText();
        return new LanguageCode(code);
    }
}
