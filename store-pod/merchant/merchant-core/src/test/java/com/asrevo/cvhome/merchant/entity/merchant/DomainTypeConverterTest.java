package com.asrevo.cvhome.merchant.entity.merchant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.asrevo.cvhome.commons.domain.DomainType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A store row with no domain type is legal, so null must survive the round trip in both directions — a converter that
 * maps it to a default silently rewrites tenant data on the next save.
 */
class DomainTypeConverterTest {

    private final DomainTypeConverter converter = new DomainTypeConverter();

    @ParameterizedTest
    @EnumSource(DomainType.class)
    void everyDomainTypeSurvivesTheRoundTrip(DomainType type) {
        assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(type))).isEqualTo(type);
    }

    @Test
    void nullStaysNullInBothDirections() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void unknownColumnValueFailsLoudly() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("NOT_A_DOMAIN_TYPE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
