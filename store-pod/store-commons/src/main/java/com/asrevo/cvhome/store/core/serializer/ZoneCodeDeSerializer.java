package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class ZoneCodeDeSerializer extends StdDeserializer<ZoneCode> {

    public ZoneCodeDeSerializer() {
        this(null);
    }

    protected ZoneCodeDeSerializer(Class<ZoneCode> t) {
        super(t);
    }

    @Override
    public ZoneCode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String code = p.getText();
        return new ZoneCode(code);
    }
}
