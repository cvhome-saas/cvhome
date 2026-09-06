package com.asrevo.cvhome.checkout.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.asrevo.cvhome.checkout.domain.CartCode;

@Converter
public class CartCodeConverter implements AttributeConverter<CartCode, String> {

    @Override
    public String convertToDatabaseColumn(CartCode code) {
        return code == null ? null : code.value();
    }

    @Override
    public CartCode convertToEntityAttribute(String value) {
        return value == null ? null : CartCode.of(value);
    }
}
