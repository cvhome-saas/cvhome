package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.commons.domain.LanguageCode;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class LanguageCodeSerializer extends StdSerializer<LanguageCode> {

    public LanguageCodeSerializer() {
        this(null);
    }

    protected LanguageCodeSerializer(Class<LanguageCode> t) {
        super(t);
    }

    @Override
    public void serialize(LanguageCode value, JsonGenerator gen, SerializationContext provider)
            throws JacksonException {
        gen.writeString(value.code());
    }

}
