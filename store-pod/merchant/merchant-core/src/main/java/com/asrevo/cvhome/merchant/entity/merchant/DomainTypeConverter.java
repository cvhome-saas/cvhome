package com.asrevo.cvhome.merchant.entity.merchant;

import com.asrevo.cvhome.commons.domain.DomainType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DomainTypeConverter implements AttributeConverter<DomainType, String> {

	@Override
	public String convertToDatabaseColumn(DomainType attribute) {
		if (attribute == null) {
			return null;
		}
		return attribute.name();
	}

	@Override
	public DomainType convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
		return DomainType.valueOf(dbData);
	}

}