package com.asrevo.cvhome.store.core.serializer;

import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class CountryIsoCodeDeSerializer extends StdDeserializer<CountryIsoCode> {

	public CountryIsoCodeDeSerializer() {
		this(null);
	}

	protected CountryIsoCodeDeSerializer(Class<CountryIsoCode> t) {
		super(t);
	}

	@Override
	public CountryIsoCode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String code = p.getText();
		return new CountryIsoCode(code);
	}

}
