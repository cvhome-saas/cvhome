package com.asrevo.cvhome.store.core.converter;

import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

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
