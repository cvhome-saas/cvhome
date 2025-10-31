package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class LanguageCodeSerializer extends StdSerializer<LanguageCode> {

	public LanguageCodeSerializer() {
		this(null);
	}

	protected LanguageCodeSerializer(Class<LanguageCode> t) {
		super(t);
	}

	@Override
	public void serialize(LanguageCode value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeString(value.code());
	}

}
