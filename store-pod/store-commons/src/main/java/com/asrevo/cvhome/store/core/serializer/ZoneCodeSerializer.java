package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class ZoneCodeSerializer extends StdSerializer<ZoneCode> {

	public ZoneCodeSerializer() {
		this(null);
	}

	protected ZoneCodeSerializer(Class<ZoneCode> t) {
		super(t);
	}

	@Override
	public void serialize(ZoneCode value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeString(value.code());
	}

}
