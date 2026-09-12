package com.asrevo.cvhome.store.core.converter;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.Sku;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkuConverterTest {

    private static final String SKU = "TSHIRT-RED-M";

    private final SkuConverter converter = new SkuConverter();

    @Test
    void aSkuIsStoredAsItsValueAndReadBack() {
        String column = converter.convertToDatabaseColumn(Sku.of(SKU));

        assertThat(column).isEqualTo(SKU);
        assertThat(converter.convertToEntityAttribute(column)).isEqualTo(Sku.of(SKU));
    }

    @Test
    void nullStaysNullInBothDirections() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void aRowThatBreaksTheRuleFailsToLoadRatherThanBeingMisread() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("abc def"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
