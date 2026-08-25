package com.asrevo.cvhome.store.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.asrevo.cvhome.commons.domain.LanguageCode;

@Converter
public class LanguageCodeConverter implements AttributeConverter<LanguageCode, String> {

    @Override
    public String convertToDatabaseColumn(LanguageCode languageCode) {
        if (languageCode == null) {
            return null;
        }
        return languageCode.code();
    }

    @Override
    public LanguageCode convertToEntityAttribute(String s) {
        return new LanguageCode(s);
    }

}
