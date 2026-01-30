package com.asrevo.cvhome.s2s.utils;

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
		if (node != null && node.get("id") != null) {
			String id = node.get("id").toString().replaceAll("\"", "");
			return new ObjectId(id);
		}
		else {
			return null;
		}
	}

}
