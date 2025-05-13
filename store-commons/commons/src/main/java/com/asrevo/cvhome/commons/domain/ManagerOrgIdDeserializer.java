package com.asrevo.cvhome.commons.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class ManagerOrgIdDeserializer extends JsonDeserializer<ManagerOrgId> {

    @Override
    public ManagerOrgId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String id = p.getValueAsString();
        return new ManagerOrgId(id);
    }
}
