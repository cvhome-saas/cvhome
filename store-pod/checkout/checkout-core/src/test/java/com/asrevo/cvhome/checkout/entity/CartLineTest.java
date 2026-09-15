package com.asrevo.cvhome.checkout.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.entity.converter.OptionLabelsConverter;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.Sku;

import static org.assertj.core.api.Assertions.assertThat;

/** A line remembers what the catalogue said for a day, and its option labels survive the round trip to one column. */
class CartLineTest {

    private static final Instant NOW = Instant.parse("2026-09-15T00:00:00Z");

    private static final Sku SKU = Sku.of("SKU-1");

    private static final String COLOR = "Color";

    private static final String RED = "Red";

    private static final String SHOE = "Shoe";

    private static final String SIZE = "Size";

    private static final String M = "M";

    private final OptionLabelsConverter converter = new OptionLabelsConverter();

    private static CartLine line() {
        Cart cart = new Cart(Orders.STORE, CartCode.of("c"), LanguageCode.defaultLanguage());
        cart.put(SKU, 1);
        return cart.line(SKU).orElseThrow();
    }

    @Test
    void aFreshLineRemembersNothingUntilTold() {
        assertThat(new CartLine().getSku()).as("JPA's constructor").isNull();
        assertThat(new Cart().getLines()).isEmpty();
        CartLine line = line();
        assertThat(line.remembers(NOW)).isFalse();

        line.remember(7L, SHOE, "shoe", "http://img/1.png", true, List.of(new OptionLabel(COLOR, RED)), NOW);

        assertThat(line.remembers(NOW)).isTrue();
        assertThat(line.remembers(NOW.plus(CartLine.SNAPSHOT_FOR))).as("a day is still trusted").isTrue();
        assertThat(line.remembers(NOW.plus(CartLine.SNAPSHOT_FOR).plus(Duration.ofSeconds(1))))
                .as("past a day the catalogue is asked again").isFalse();
        assertThat(line.getProductId()).isEqualTo(7L);
        assertThat(line.getOptionLabels()).containsExactly(new OptionLabel(COLOR, RED));

        line.remember(7L, SHOE, null, null, false, null, NOW);
        assertThat(line.getOptionLabels()).isEmpty();
        assertThat(line.getCatalogAvailable()).isFalse();
    }

    @Test
    void optionLabelsSurviveTheColumnAndAnEmptyListIsNull() {
        List<OptionLabel> labels = List.of(new OptionLabel(COLOR, RED), new OptionLabel(SIZE, ""),
                new OptionLabel(null, M));

        String column = converter.convertToDatabaseColumn(labels);

        assertThat(column).doesNotContain("\n", ",");
        assertThat(converter.convertToEntityAttribute(column)).containsExactly(new OptionLabel(COLOR, RED),
                new OptionLabel(SIZE, ""), new OptionLabel("", M));
        assertThat(converter.convertToDatabaseColumn(List.of())).isNull();
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
        assertThat(converter.convertToEntityAttribute(SIZE)).containsExactly(new OptionLabel(SIZE, ""));
    }
}
