package com.asrevo.cvhome.store.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.asrevo.cvhome.commons.domain.ZoneCode;

@Converter
public class ZoneCodeConverter implements AttributeConverter<ZoneCode, String> {

    @Override
    public String convertToDatabaseColumn(ZoneCode zoneCode) {
        if (zoneCode == null) {
            return null;
        }
        return zoneCode.code();
    }

    @Override
    public ZoneCode convertToEntityAttribute(String s) {
        return new ZoneCode(s);
    }

}
