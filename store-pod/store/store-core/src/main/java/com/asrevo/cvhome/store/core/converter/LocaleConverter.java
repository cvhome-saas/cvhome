package com.asrevo.cvhome.store.core.converter;

import jakarta.persistence.AttributeConverter;
import java.util.Locale;

public class LocaleConverter implements AttributeConverter<Locale, String> {

    @Override
    public String convertToDatabaseColumn(Locale locale) {
        if (locale != null) {
            return locale.toLanguageTag();
        }
        return null;
    }

    @Override
    public Locale convertToEntityAttribute(String languageTag) {
        if (languageTag != null && !languageTag.isEmpty()) {
            return Locale.forLanguageTag(languageTag);
        }
        return null;
    }
}
