package com.asrevo.cvhome.catalog.services.option;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.asrevo.cvhome.catalog.entity.ProductOption;
import com.asrevo.cvhome.catalog.entity.ProductOptionDescription;
import com.asrevo.cvhome.catalog.entity.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.ProductOptionValueDescription;
import com.asrevo.cvhome.catalog.model.option.PersistableProductOption;
import com.asrevo.cvhome.catalog.model.option.PersistableProductOptionValue;
import com.asrevo.cvhome.catalog.model.option.ReadableProductOption;
import com.asrevo.cvhome.catalog.model.option.ReadableProductOptionValue;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public final class ProductOptionMapper {

    private ProductOptionMapper() {
    }

    public static ReadableProductOption toReadable(ProductOption option, LanguageCode language,
                                                   boolean allLanguages) {
        ReadableProductOption readable = new ReadableProductOption();
        readable.setId(option.getId());
        readable.setCode(option.getCode());
        readable.setSortOrder(option.getSortOrder());
        if (LanguageCode.isLanguage(language)) {
            /*
             * Falls back to the code, like ProductVariantMapper.label does for the same pair. Left null,
             * a store language with no option description rendered an empty legend and an empty
             * aria-label on the storefront's chip rail — and the field is non-optional in the client's
             * types, so nothing there could have caught it.
             */
            readable.setName(option.description(language).map(ProductOptionMapper::description)
                    .map(d -> d.getName()).orElse(option.getCode()));
        }
        if (allLanguages) {
            readable.setDescriptions(option.getDescriptions().stream()
                    .map(ProductOptionMapper::description).toList());
        }
        readable.setValues(option.getValues().stream()
                .sorted(Comparator.comparing(ProductOptionValue::getSortOrder,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ProductOptionValue::getId))
                .map(value -> toReadable(value, language, allLanguages)).toList());
        return readable;
    }

    public static ReadableProductOptionValue toReadable(ProductOptionValue value, LanguageCode language,
                                                        boolean allLanguages) {
        ReadableProductOptionValue readable = new ReadableProductOptionValue();
        readable.setId(value.getId());
        readable.setCode(value.getCode());
        readable.setSortOrder(value.getSortOrder());
        if (LanguageCode.isLanguage(language)) {
            value.description(language).ifPresent(d -> readable.setName(d.getName()));
        }
        if (allLanguages) {
            readable.setDescriptions(value.getDescriptions().stream()
                    .map(ProductOptionMapper::description).toList());
        }
        return readable;
    }

    /**
     * The values list replaces the option's whole value set: an entry with an id edits that row, without one it is
     * created, and rows absent from the write are removed (orphanRemoval).
     */
    public static void apply(PersistableProductOption source, ProductOption target) {
        target.setSortOrder(source.getSortOrder());
        applyDescriptions(source, target);
        Map<Long, ProductOptionValue> existing = new HashMap<>();
        target.getValues().forEach(v -> existing.put(v.getId(), v));
        target.getValues().clear();
        for (PersistableProductOptionValue value : source.getValues()) {
            ProductOptionValue entity = existing.getOrDefault(value.getId(), new ProductOptionValue(target));
            apply(value, entity);
            target.getValues().add(entity);
        }
    }

    private static void applyDescriptions(PersistableProductOption source, ProductOption target) {
        Map<LanguageCode, ProductOptionDescription> existing = new HashMap<>();
        target.getDescriptions().forEach(d -> existing.put(d.getLanguageCode(), d));
        target.getDescriptions().clear();
        for (com.asrevo.cvhome.catalog.model.option.ProductOptionDescription d : source.getDescriptions()) {
            ProductOptionDescription entity =
                    existing.getOrDefault(d.getLanguage(), new ProductOptionDescription(target));
            entity.setLanguageCode(d.getLanguage());
            entity.setName(d.getName());
            entity.setTitle(d.getTitle());
            entity.setDescription(d.getDescription());
            target.getDescriptions().add(entity);
        }
    }

    private static void apply(PersistableProductOptionValue source, ProductOptionValue target) {
        target.setCode(source.getCode());
        target.setSortOrder(source.getSortOrder());
        Map<LanguageCode, ProductOptionValueDescription> existing = new HashMap<>();
        target.getDescriptions().forEach(d -> existing.put(d.getLanguageCode(), d));
        target.getDescriptions().clear();
        for (com.asrevo.cvhome.catalog.model.option.ProductOptionDescription d : source.getDescriptions()) {
            ProductOptionValueDescription entity =
                    existing.getOrDefault(d.getLanguage(), new ProductOptionValueDescription(target));
            entity.setLanguageCode(d.getLanguage());
            entity.setName(d.getName());
            entity.setTitle(d.getTitle());
            entity.setDescription(d.getDescription());
            target.getDescriptions().add(entity);
        }
    }

    private static com.asrevo.cvhome.catalog.model.option.ProductOptionDescription description(
            ProductOptionDescription d) {
        var readable = new com.asrevo.cvhome.catalog.model.option.ProductOptionDescription();
        readable.setId(d.getId());
        readable.setLanguage(d.getLanguageCode());
        readable.setName(d.getName());
        readable.setTitle(d.getTitle());
        readable.setDescription(d.getDescription());
        return readable;
    }

    private static com.asrevo.cvhome.catalog.model.option.ProductOptionDescription description(
            ProductOptionValueDescription d) {
        var readable = new com.asrevo.cvhome.catalog.model.option.ProductOptionDescription();
        readable.setId(d.getId());
        readable.setLanguage(d.getLanguageCode());
        readable.setName(d.getName());
        readable.setTitle(d.getTitle());
        readable.setDescription(d.getDescription());
        return readable;
    }

    /**
     * Duplicate value codes inside one write would violate the DB constraint with an opaque 500; spotted here first.
     */
    public static boolean hasDuplicateValueCodes(PersistableProductOption source) {
        return source.getValues().stream().map(PersistableProductOptionValue::getCode)
                .filter(Objects::nonNull).distinct().count() != source.getValues().size();
    }
}
