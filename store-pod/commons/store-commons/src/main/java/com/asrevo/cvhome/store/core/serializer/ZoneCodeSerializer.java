package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class ZoneCodeSerializer extends StdSerializer<ZoneCode> {

	public ZoneCodeSerializer() {
		this(null);
	}

	protected ZoneCodeSerializer(Class<ZoneCode> t) {
		super(t);
	}

	@Override
	public void serialize(ZoneCode value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
		gen.writeString(value.code());
	}

}
