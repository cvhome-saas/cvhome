package com.asrevo.cvhome.store.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;

@Converter
public class CountryIsoCodeConverter implements AttributeConverter<CountryIsoCode, String> {

    @Override
    public String convertToDatabaseColumn(CountryIsoCode countryIsoCode) {
        if (countryIsoCode == null) {
            return null;
        }
        return countryIsoCode.isoCode();
    }

    @Override
    public CountryIsoCode convertToEntityAttribute(String s) {
        return new CountryIsoCode(s);
    }

}
