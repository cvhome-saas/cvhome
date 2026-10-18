package com.asrevo.cvhome.commons.domain;

import java.util.List;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The four numeric catalog ids share one rule (a positive number) and one wire shape (a JSON number, read back from
 * a number or a digit string), and each is its own type: a product id is not a category id.
 */
class CatalogIdsTest {

    private static final long SEVEN = 7L;

    private static final String DIGITS = "7";

    private static final String A_1 = "A-1";

    private final ObjectMapper mapper = new ObjectMapper();

    static Stream<Arguments> ids() {
        return Stream.of(
                Arguments.of(ProductId.class, (LongFunction<Object>) ProductId::of, (Function<String, Object>) ProductId::of),
                Arguments.of(CategoryId.class, (LongFunction<Object>) CategoryId::of,
                        (Function<String, Object>) CategoryId::of),
                Arguments.of(ManufacturerId.class, (LongFunction<Object>) ManufacturerId::of,
                        (Function<String, Object>) ManufacturerId::of),
                Arguments.of(VariantId.class, (LongFunction<Object>) VariantId::of, (Function<String, Object>) VariantId::of));
    }

    @ParameterizedTest
    @MethodSource("ids")
    void aPositiveNumberIsAnIdAndReadsAsItsDigits(Class<?> type, LongFunction<Object> ofLong,
                                                  Function<String, Object> ofString) throws Exception {
        Object id = ofLong.apply(SEVEN);

        assertThat(id).isEqualTo(ofString.apply(" 7 ")).isInstanceOf(Identifier.class).isInstanceOf(KeyPart.class);
        assertThat(((Identifier) id).getId()).isEqualTo(SEVEN);
        assertThat(((KeyPart) id).cacheKeyPart()).isEqualTo(DIGITS);
        assertThat(id).hasToString(DIGITS);
        assertThat(mapper.writeValueAsString(id)).isEqualTo(DIGITS);
        assertThat(mapper.readValue(DIGITS, type)).isEqualTo(id);
        assertThat(mapper.readValue("\"7\"", type)).isEqualTo(id);
    }

    @ParameterizedTest
    @MethodSource("ids")
    void zeroANegativeNumberAndTextAreRefused(Class<?> type, LongFunction<Object> ofLong,
                                              Function<String, Object> ofString) {
        assertThatThrownBy(() -> ofLong.apply(0L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ofLong.apply(-1L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ofString.apply("seven")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mapper.readValue("0", type)).isInstanceOf(JacksonException.class);
        assertThatThrownBy(() -> mapper.readValue("\"x\"", type)).isInstanceOf(JacksonException.class);
        assertThatThrownBy(() -> mapper.readValue("true", type)).isInstanceOf(JacksonException.class);
    }

    @Test
    void eachIdIsItsOwnTypeAndOrdersByValue() {
        assertThat(ProductId.of(SEVEN)).isNotEqualTo(CategoryId.of(SEVEN));
        assertThat(new ProductId(SEVEN)).isEqualTo(ProductId.of(SEVEN));
        assertThat(List.of(VariantId.of(9), VariantId.of(SEVEN)).stream().sorted().toList())
                .containsExactly(VariantId.of(SEVEN), VariantId.of(9));
        assertThat(ManufacturerId.of(SEVEN).compareTo(ManufacturerId.of(9))).isNegative();
        assertThat(Sku.of(A_1).cacheKeyPart()).isEqualTo(A_1);
        assertThatThrownBy(() -> new ProductId(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
