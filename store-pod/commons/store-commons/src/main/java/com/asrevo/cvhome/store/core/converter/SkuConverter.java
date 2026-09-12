package com.asrevo.cvhome.store.core.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.asrevo.cvhome.commons.domain.Sku;

/**
 * Reads through {@link Sku}'s constructor, so a row that breaks the sku rule fails to load instead of being carried
 * along as if it were one.
 */
@Converter
public class SkuConverter implements AttributeConverter<Sku, String> {

    @Override
    public String convertToDatabaseColumn(Sku sku) {
        return sku == null ? null : sku.value();
    }

    @Override
    public Sku convertToEntityAttribute(String value) {
        return value == null ? null : Sku.of(value);
    }
}
