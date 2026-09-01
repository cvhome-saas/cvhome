package com.asrevo.cvhome.catalog.services.variant;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductOption;
import com.asrevo.cvhome.catalog.entity.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.ProductVariant;
import com.asrevo.cvhome.catalog.entity.ProductVariantOptionValue;
import com.asrevo.cvhome.catalog.model.option.ReadableProductOption;
import com.asrevo.cvhome.catalog.model.product.ReadableProductVariant;
import com.asrevo.cvhome.catalog.model.product.ReadableProductVariantDefinition;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantOptionValue;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantSelection;
import com.asrevo.cvhome.catalog.services.option.ProductOptionMapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;

/**
 * Variant entities to their three wire shapes: the storefront's {@code variants[]} element, the console matrix
 * row (with labels), and the sku-addressed {@code variant} selection block a cart or order line renders.
 */
public final class ProductVariantMapper {

    public static final Comparator<ProductVariant> DISPLAY_ORDER =
            Comparator.comparingInt(ProductVariant::getSortOrder)
                    .thenComparing(ProductVariant::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private ProductVariantMapper() {
    }

    public static ReadableProductVariant toReadable(ProductVariant variant) {
        return fill(new ReadableProductVariant(), variant);
    }

    public static ReadableProductVariantDefinition toDefinition(ProductVariant variant, LanguageCode language) {
        ReadableProductVariantDefinition definition = fill(new ReadableProductVariantDefinition(), variant);
        definition.setOptionValues(labels(variant, language));
        return definition;
    }

    /**
     * The label block of a sku-addressed read — null for a default variant, which has nothing selected.
     */
    public static ReadableVariantSelection toSelection(ProductVariant variant, LanguageCode language) {
        if (variant.getOptionValues().isEmpty()) {
            return null;
        }
        ReadableVariantSelection selection = new ReadableVariantSelection();
        selection.setSku(variant.getSku());
        selection.setOptionValues(labels(variant, language));
        return selection;
    }

    /**
     * The product page's {@code options[]}: the product's axes in assignment order, each carrying only the
     * values its variants actually use — no dead chips. Takes the already-hydrated variant list so no lazy
     * collection is touched per variant.
     */
    public static List<ReadableProductOption> toOptions(Product product, List<ProductVariant> hydratedVariants,
                                                        LanguageCode language) {
        Map<Long, ProductOption> assigned = new LinkedHashMap<>();
        product.getOptionAssignments().stream()
                .sorted(Comparator.comparingInt(a -> a.getSortOrder()))
                .forEach(a -> assigned.put(a.getOption().getId(), a.getOption()));
        Map<Long, java.util.Set<Long>> usedValues = new LinkedHashMap<>();
        for (ProductVariant variant : hydratedVariants) {
            for (ProductVariantOptionValue chosen : variant.getOptionValues()) {
                usedValues.computeIfAbsent(chosen.getKey().getOptionId(), k -> new java.util.HashSet<>())
                        .add(chosen.getOptionValue().getId());
            }
        }
        return assigned.values().stream().map(option -> {
            ReadableProductOption readable = ProductOptionMapper.toReadable(option, language, false);
            var used = usedValues.getOrDefault(option.getId(), java.util.Set.of());
            readable.setValues(readable.getValues().stream()
                    .filter(value -> used.contains(value.getId())).toList());
            return readable;
        }).toList();
    }

    private static <T extends ReadableProductVariant> T fill(T readable, ProductVariant variant) {
        readable.setId(variant.getId());
        readable.setSku(variant.getSku());
        readable.setSortOrder(variant.getSortOrder());
        readable.setDefaultVariant(variant.isDefaultVariant());
        readable.setOptionValueIds(variant.optionValueIds());
        return readable;
    }

    private static List<ReadableVariantOptionValue> labels(ProductVariant variant, LanguageCode language) {
        return variant.getOptionValues().stream()
                .sorted(Comparator.comparing(chosen -> sortKey(chosen.getOptionValue().getOption())))
                .map(chosen -> label(chosen.getOptionValue(), language)).toList();
    }

    private static ReadableVariantOptionValue label(ProductOptionValue value, LanguageCode language) {
        ProductOption option = value.getOption();
        ReadableVariantOptionValue label = new ReadableVariantOptionValue();
        label.setOptionId(option.getId());
        label.setOptionCode(option.getCode());
        label.setOptionName(option.description(language).map(d -> d.getName()).orElse(option.getCode()));
        label.setValueId(value.getId());
        label.setValueCode(value.getCode());
        label.setValueName(value.description(language).map(d -> d.getName()).orElse(value.getCode()));
        label.setSortOrder(option.getSortOrder());
        return label;
    }

    private static int sortKey(ProductOption option) {
        return option.getSortOrder() == null ? Integer.MAX_VALUE : option.getSortOrder();
    }
}
