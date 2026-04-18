package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.ZoneCode;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class ZoneCodeDeSerializer extends StdDeserializer<ZoneCode> {

    public ZoneCodeDeSerializer() {
        super(ZoneCode.class);
    }

    protected ZoneCodeDeSerializer(Class<ZoneCode> t) {
        super(t);
    }

    @Override
    public ZoneCode deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String code = p.getString();
        return new ZoneCode(code);
    }

}
