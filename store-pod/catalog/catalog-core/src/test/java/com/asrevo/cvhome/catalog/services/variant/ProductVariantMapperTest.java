package com.asrevo.cvhome.catalog.services.variant;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductOption;
import com.asrevo.cvhome.catalog.entity.ProductOptionAssignment;
import com.asrevo.cvhome.catalog.entity.ProductOptionDescription;
import com.asrevo.cvhome.catalog.entity.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.ProductOptionValueDescription;
import com.asrevo.cvhome.catalog.entity.ProductVariant;
import com.asrevo.cvhome.catalog.entity.ProductVariantOptionValue;
import com.asrevo.cvhome.catalog.model.option.ReadableProductOption;
import com.asrevo.cvhome.catalog.model.product.ReadableProductVariant;
import com.asrevo.cvhome.catalog.model.product.ReadableProductVariantDefinition;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantOptionValue;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantSelection;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The three wire shapes a variant becomes: the storefront's {@code variants[]} element, the console matrix row
 * with resolved labels, and the sku-addressed selection block a cart or order line renders as "Color: Red".
 */
class ProductVariantMapperTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final LanguageCode EN = new LanguageCode("en");

    private static final LanguageCode FR = new LanguageCode("fr");

    private static final String COLOR = "color";

    private static final String SIZE = "size";

    private static final String RED = "red";

    private static final String SKU = "SKU-RED-M";

    private static ProductOption option(long id, String code, Integer sortOrder, String enName, String frName) {
        ProductOption option = new ProductOption();
        option.setId(id);
        option.setStoreMerchantId(STORE);
        option.setCode(code);
        option.setSortOrder(sortOrder);
        if (enName != null) {
            ProductOptionDescription en = new ProductOptionDescription();
            en.setLanguageCode(EN);
            en.setName(enName);
            en.setOption(option);
            option.getDescriptions().add(en);
        }
        if (frName != null) {
            ProductOptionDescription fr = new ProductOptionDescription();
            fr.setLanguageCode(FR);
            fr.setName(frName);
            fr.setOption(option);
            option.getDescriptions().add(fr);
        }
        return option;
    }

    private static ProductOptionValue value(ProductOption option, long id, String code, String enName) {
        ProductOptionValue value = new ProductOptionValue(option);
        value.setId(id);
        value.setCode(code);
        if (enName != null) {
            ProductOptionValueDescription description = new ProductOptionValueDescription();
            description.setLanguageCode(EN);
            description.setName(enName);
            description.setOptionValue(value);
            value.getDescriptions().add(description);
        }
        option.getValues().add(value);
        return value;
    }

    private static ProductVariant variant(long id, String sku, int sortOrder, boolean isDefault,
                                          ProductOptionValue... chosen) {
        ProductVariant variant = new ProductVariant();
        variant.setId(id);
        variant.setSku(sku);
        variant.setSortOrder(sortOrder);
        variant.setDefaultVariant(isDefault);
        for (ProductOptionValue value : chosen) {
            variant.getOptionValues().add(new ProductVariantOptionValue(variant, value.getOption(), value));
        }
        return variant;
    }

    @Test
    void readableCarriesTheSellableFactsAndNothingElse() {
        ProductOption color = option(1L, COLOR, 0, "Color", null);
        ProductVariant variant = variant(50L, SKU, 2, true, value(color, 11L, RED, "Red"));

        ReadableProductVariant readable = ProductVariantMapper.toReadable(variant);

        assertThat(readable.getId()).isEqualTo(50L);
        assertThat(readable.getSku()).isEqualTo(SKU);
        assertThat(readable.getSortOrder()).isEqualTo(2);
        assertThat(readable.isDefaultVariant()).isTrue();
        assertThat(readable.getOptionValueIds()).containsExactly(11L);
    }

    @Test
    void definitionResolvesLabelsInTheAskedLanguageAndOrdersThemByOptionSortOrder() {
        // size sorts first deliberately, so the assertion proves the ordering is the option's, not insertion's.
        ProductOption size = option(2L, SIZE, 0, "Size", "Taille");
        ProductOption color = option(1L, COLOR, 1, "Color", "Couleur");
        ProductVariant variant = variant(51L, SKU, 0, false,
                value(color, 11L, RED, "Red"), value(size, 21L, "m", "M"));

        ReadableProductVariantDefinition definition = ProductVariantMapper.toDefinition(variant, EN);

        assertThat(definition.getSku()).isEqualTo(SKU);
        assertThat(definition.getOptionValues())
                .extracting(ReadableVariantOptionValue::getOptionCode)
                .containsExactly(SIZE, COLOR);
        ReadableVariantOptionValue colorLabel = definition.getOptionValues().get(1);
        assertThat(colorLabel.getOptionName()).isEqualTo("Color");
        assertThat(colorLabel.getValueId()).isEqualTo(11L);
        assertThat(colorLabel.getValueName()).isEqualTo("Red");
    }

    @Test
    void aLabelFallsBackToItsCodeWhenTheLanguageHasNoCopy() {
        // The storefront must never render an empty chip because a merchant skipped a translation.
        ProductOption color = option(1L, COLOR, 0, "Color", null);
        ProductVariant variant = variant(52L, SKU, 0, false, value(color, 11L, RED, "Red"));

        ReadableVariantOptionValue label = ProductVariantMapper.toDefinition(variant, FR).getOptionValues()
                .getFirst();

        assertThat(label.getOptionName()).isEqualTo(COLOR);
        assertThat(label.getValueName()).isEqualTo(RED);
    }

    @Test
    void selectionIsNullForADefaultVariantBecauseNothingWasSelected() {
        ProductVariant simple = variant(53L, "SKU-SIMPLE", 0, true);

        assertThat(ProductVariantMapper.toSelection(simple, EN)).isNull();
    }

    @Test
    void selectionCarriesTheSkuAndItsLabelsForACombination() {
        ProductOption color = option(1L, COLOR, 0, "Color", null);
        ProductVariant variant = variant(54L, SKU, 0, false, value(color, 11L, RED, "Red"));

        ReadableVariantSelection selection = ProductVariantMapper.toSelection(variant, EN);

        assertThat(selection).isNotNull();
        assertThat(selection.getSku()).isEqualTo(SKU);
        assertThat(selection.getOptionValues()).singleElement()
                .satisfies(label -> {
                    assertThat(label.getOptionName()).isEqualTo("Color");
                    assertThat(label.getValueName()).isEqualTo("Red");
                });
    }

    @Test
    void optionsAreTheProductsAxesInAssignmentOrderCarryingOnlyTheValuesItsVariantsUse() {
        /*
         * The "no dead chips" rule: the store's Color has three values, but this product only sells red and
         * blue, so green must not reach the PDP — a chip that resolves to no variant is unpickable.
         */
        ProductOption color = option(1L, COLOR, 0, "Color", null);
        ProductOptionValue red = value(color, 11L, RED, "Red");
        ProductOptionValue blue = value(color, 12L, "blue", "Blue");
        value(color, 13L, "green", "Green");
        ProductOption size = option(2L, SIZE, 0, "Size", null);
        ProductOptionValue medium = value(size, 21L, "m", "M");

        Product product = new Product();
        product.setId(7L);
        // size is assigned second, so it must come second whatever the option's own sort order says
        product.getOptionAssignments().add(new ProductOptionAssignment(product, color, 0));
        product.getOptionAssignments().add(new ProductOptionAssignment(product, size, 1));

        List<ProductVariant> variants = List.of(
                variant(60L, "SKU-RED-M", 0, true, red, medium),
                variant(61L, "SKU-BLUE-M", 1, false, blue, medium));

        List<ReadableProductOption> options = ProductVariantMapper.toOptions(product, variants, EN);

        assertThat(options).extracting(ReadableProductOption::getCode).containsExactly(COLOR, SIZE);
        assertThat(options.getFirst().getValues())
                .extracting(value -> value.getCode())
                .containsExactlyInAnyOrder(RED, "blue");
        assertThat(options.get(1).getValues()).singleElement()
                .extracting(value -> value.getCode()).isEqualTo("m");
    }

    @Test
    void displayOrderSortsBySortOrderThenId() {
        ProductVariant second = variant(2L, "B", 1, false);
        ProductVariant first = variant(1L, "A", 0, true);
        ProductVariant third = variant(3L, "C", 1, false);

        assertThat(List.of(third, second, first).stream().sorted(ProductVariantMapper.DISPLAY_ORDER).toList())
                .extracting(ProductVariant::getSku)
                .containsExactly("A", "B", "C");
    }
}
