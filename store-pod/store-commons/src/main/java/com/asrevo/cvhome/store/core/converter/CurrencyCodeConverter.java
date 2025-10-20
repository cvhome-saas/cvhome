package com.asrevo.cvhome.store.core.converter;

import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CurrencyCodeConverter implements AttributeConverter<CurrencyCode, String> {

	@Override
	public String convertToDatabaseColumn(CurrencyCode currencyCode) {
		if (currencyCode == null) {
			return null;
		}
		return currencyCode.code();
	}

	@Override
	public CurrencyCode convertToEntityAttribute(String s) {
		return new CurrencyCode(s);
	}

}
