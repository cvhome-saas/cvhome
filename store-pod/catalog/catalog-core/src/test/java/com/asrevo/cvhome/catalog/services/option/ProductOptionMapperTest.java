package com.asrevo.cvhome.catalog.services.option;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.catalog.entity.ProductOption;
import com.asrevo.cvhome.catalog.entity.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.ProductOptionValueDescription;
import com.asrevo.cvhome.catalog.model.option.PersistableProductOption;
import com.asrevo.cvhome.catalog.model.option.PersistableProductOptionValue;
import com.asrevo.cvhome.catalog.model.option.ProductOptionDescription;
import com.asrevo.cvhome.catalog.model.option.ReadableProductOption;
import com.asrevo.cvhome.commons.domain.LanguageCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The whole-document write: values with ids keep their rows, values without are created, absent ones drop out, and
 * the readable side resolves the requested language and sorts values by sort order.
 */
class ProductOptionMapperTest {

    private static final LanguageCode EN = new LanguageCode("en");

    private static final LanguageCode FR = new LanguageCode("fr");

    private static final String RED = "red";

    private static final String RED_NAME = "Red";

    private static final String BLUE = "blue";

    private static final String BLUE_NAME = "Blue";

    private static final String CRIMSON = "crimson";

    private static final String GREEN = "green";

    private static final String COULEUR = "Couleur";

    private static ProductOptionDescription description(LanguageCode language, String name) {
        ProductOptionDescription d = new ProductOptionDescription();
        d.setLanguage(language);
        d.setName(name);
        return d;
    }

    private static PersistableProductOptionValue value(Long id, String code, Integer sortOrder, String name) {
        PersistableProductOptionValue v = new PersistableProductOptionValue();
        v.setId(id);
        v.setCode(code);
        v.setSortOrder(sortOrder);
        v.getDescriptions().add(description(EN, name));
        return v;
    }

    private static PersistableProductOption option(PersistableProductOptionValue... values) {
        PersistableProductOption o = new PersistableProductOption();
        o.setCode("color");
        o.setSortOrder(0);
        o.getDescriptions().add(description(EN, "Color"));
        o.getDescriptions().add(description(FR, COULEUR));
        o.setValues(List.of(values));
        return o;
    }

    @Test
    void applyCreatesValuesWithTheirTranslations() {
        ProductOption target = new ProductOption();

        ProductOptionMapper.apply(option(value(null, RED, 1, RED_NAME), value(null, BLUE, 0, BLUE_NAME)), target);

        assertThat(target.getValues()).hasSize(2);
        assertThat(target.getDescriptions()).hasSize(2);
        ProductOptionValue red = target.getValues().stream()
                .filter(v -> RED.equals(v.getCode())).findFirst().orElseThrow();
        assertThat(red.getOption()).isSameAs(target);
        assertThat(red.getDescriptions()).extracting(ProductOptionValueDescription::getName).containsExactly(RED_NAME);
    }

    @Test
    void applyKeepsRowsAddressedByIdAndDropsAbsentOnes() {
        ProductOption target = new ProductOption();
        ProductOptionMapper.apply(option(value(null, RED, 0, RED_NAME), value(null, BLUE, 1, BLUE_NAME)), target);
        ProductOptionValue red = target.getValues().stream()
                .filter(v -> RED.equals(v.getCode())).findFirst().orElseThrow();
        red.setId(71L);

        ProductOptionMapper.apply(option(value(71L, CRIMSON, 0, "Crimson"), value(null, GREEN, 1, "Green")),
                target);

        assertThat(target.getValues()).hasSize(2);
        ProductOptionValue kept = target.getValues().stream()
                .filter(v -> CRIMSON.equals(v.getCode())).findFirst().orElseThrow();
        assertThat(kept).as("the id-addressed row is edited in place").isSameAs(red);
        assertThat(target.getValues()).extracting(ProductOptionValue::getCode)
                .containsExactlyInAnyOrder(CRIMSON, GREEN);
    }

    @Test
    void toReadableResolvesTheLanguageAndSortsValues() {
        ProductOption target = new ProductOption();
        target.setId(7L);
        ProductOptionMapper.apply(option(value(null, RED, 1, RED_NAME), value(null, BLUE, 0, BLUE_NAME)), target);

        ReadableProductOption readable = ProductOptionMapper.toReadable(target, FR, true);

        assertThat(readable.getName()).isEqualTo(COULEUR);
        assertThat(readable.getDescriptions()).hasSize(2);
        assertThat(readable.getValues()).extracting(v -> v.getCode()).containsExactly(BLUE, RED);
        assertThat(readable.getValues().getFirst().getName()).as("no fr copy of the value").isNull();
    }

    @Test
    void duplicateValueCodesInsideOneWriteAreDetected() {
        assertThat(ProductOptionMapper.hasDuplicateValueCodes(
                option(value(null, RED, 0, RED_NAME), value(null, RED, 1, "Red again")))).isTrue();
        assertThat(ProductOptionMapper.hasDuplicateValueCodes(
                option(value(null, RED, 0, RED_NAME), value(null, BLUE, 1, BLUE_NAME)))).isFalse();
    }
}
