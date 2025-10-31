package com.asrevo.cvhome.store.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter
public class LocaleConverter implements AttributeConverter<Locale, String> {

	@Override
	public String convertToDatabaseColumn(Locale locale) {
		if (locale == null) {
			return null;
		}
		return locale.toLanguageTag();
	}

	@Override
	public Locale convertToEntityAttribute(String languageTag) {
		if (languageTag != null && !languageTag.isEmpty()) {
			return Locale.forLanguageTag(languageTag);
		}
		return null;
	}

}
