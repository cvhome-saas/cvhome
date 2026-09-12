package com.asrevo.cvhome.commons.domain;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The sku's one rule, and the wire shape every pod and both frontends rely on.
 *
 * <p>
 * The rule used to live only on catalog's request DTOs, so inventory stocked skus catalog could never create. It is
 * pinned here from both ends: the constructor and {@link Sku#FORMAT} — what a request body's {@code @Pattern} uses —
 * must accept and refuse exactly the same strings, or the rule is two rules again.
 * </p>
 */
class SkuTest {

    private static final String SKU = "K6-SKU_0001";
    private static final String BARE = "\"%s\"".formatted(SKU);
    private static final String LONGEST = "A".repeat(255);

    private final ObjectMapper mapper = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(strings = {SKU, "a", "0", "_", "-", "abc-DEF_123"})
    void lettersDigitsUnderscoreAndDashAreASku(String candidate) {
        assertThat(new Sku(candidate).value()).isEqualTo(candidate);
        assertThat(candidate).matches(Sku.FORMAT);
    }

    @Test
    void twoHundredFiftyFiveCharactersFitTheColumn() {
        assertThat(new Sku(LONGEST).value()).hasSize(255);
        assertThat(LONGEST).matches(Sku.FORMAT);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "abc def", "ABC ", " ABC", "a.b", "a/b", "a,b", "é", "ABC\n"})
    void anythingElseIsRefusedByTheConstructorAndByTheFormat(String candidate) {
        assertThatThrownBy(() -> new Sku(candidate)).isInstanceOf(IllegalArgumentException.class);
        if (candidate != null) {
            assertThat(candidate).doesNotMatch(Sku.FORMAT);
        }
    }

    @Test
    void twoHundredFiftySixCharactersDoNotFitTheColumn() {
        String tooLong = LONGEST.concat("Z");
        assertThatThrownBy(() -> new Sku(tooLong)).isInstanceOf(IllegalArgumentException.class);
        assertThat(tooLong).doesNotMatch(Sku.FORMAT);
    }

    @Test
    void caseIsKeptBecauseTheUniqueKeysCompareExactly() {
        assertThat(new Sku("abc")).isNotEqualTo(new Sku("ABC"));
    }

    @Test
    void itWritesABareString() {
        assertThat(mapper.writeValueAsString(Sku.of(SKU))).isEqualTo(BARE);
    }

    @Test
    void itReadsABareString() {
        assertThat(mapper.readValue(BARE, Sku.class)).isEqualTo(Sku.of(SKU));
    }

    @Test
    void aFieldAndAListKeepTheBareShape() {
        Line line = new Line(Sku.of(SKU), List.of(Sku.of(SKU)));
        String json = mapper.writeValueAsString(line);

        assertThat(json).isEqualTo("{\"sku\":%s,\"skus\":[%s]}".formatted(BARE, BARE));
        assertThat(mapper.readValue(json, Line.class)).isEqualTo(line);
    }

    @Test
    void aMalformedValueOnTheWireIsRefusedRatherThanCarried() {
        assertThatThrownBy(() -> mapper.readValue("\"abc def\"", Sku.class)).isInstanceOf(JacksonException.class);
    }

    /**
     * Inside a body is where a malformed sku actually arrives. It must fail as Jackson's own error — Spring turns that
     * into a 400 — and not as the constructor's {@code IllegalArgumentException}, which would escape as a 500.
     */
    @Test
    void aMalformedValueInsideABodyIsAJacksonErrorNamingTheField() {
        assertThatThrownBy(() -> mapper.readValue("{\"sku\":\"abc def\",\"skus\":[]}", Line.class))
                .isInstanceOf(JacksonException.class).hasMessageContaining("sku");
    }

    @Test
    void aNumberIsNotASku() {
        assertThatThrownBy(() -> mapper.readValue("42", Sku.class)).isInstanceOf(JacksonException.class);
    }

    @Test
    void anExplicitJsonNullReadsAsNull() {
        assertThat(mapper.readValue("{\"sku\":null,\"skus\":[]}", Line.class).sku()).isNull();
    }

    @Test
    void toStringIsTheValueBecauseThatIsWhatAQueryParameterCarries() {
        assertThat(Sku.of(SKU)).hasToString(SKU);
    }

    @Test
    void skusSortAsTheirStringsSoInventoryKeepsItsLockOrder() {
        List<String> values = List.of("b-2", "B-10", "a_1", "A-9", "b-10");

        assertThat(values.stream().map(Sku::of).sorted().map(Sku::value).toList())
                .isEqualTo(values.stream().sorted().toList());
    }

    record Line(Sku sku, List<Sku> skus) {
    }
}
