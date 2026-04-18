package com.asrevo.cvhome.s2s.utils;

import java.util.Optional;

import org.bson.types.ObjectId;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class ObjectIdDeserializer extends StdDeserializer<ObjectId> {

    public ObjectIdDeserializer() {
        this(ObjectId.class);
    }

    public ObjectIdDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ObjectId deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws JacksonException {
        TreeNode node = jsonParser.readValueAsTree();
        return Optional.ofNullable(node)
                .flatMap(n -> Optional.ofNullable(n.get("id")))
                .map(n -> n.toString().replace("\"", ""))
                .map(ObjectId::new)
                .orElse(null);
    }

}
