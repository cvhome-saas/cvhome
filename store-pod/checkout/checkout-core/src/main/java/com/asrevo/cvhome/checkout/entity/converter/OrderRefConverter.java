package com.asrevo.cvhome.checkout.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.asrevo.cvhome.checkout.domain.OrderRef;

@Converter
public class OrderRefConverter implements AttributeConverter<OrderRef, String> {

    @Override
    public String convertToDatabaseColumn(OrderRef ref) {
        return ref == null ? null : ref.value();
    }

    @Override
    public OrderRef convertToEntityAttribute(String value) {
        return value == null ? null : OrderRef.of(value);
    }
}
