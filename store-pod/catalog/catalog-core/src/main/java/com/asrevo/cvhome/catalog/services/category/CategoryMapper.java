package com.asrevo.cvhome.catalog.services.category;

import java.util.HashMap;
import java.util.Map;

import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.CategoryDescription;
import com.asrevo.cvhome.catalog.model.category.CategoryReference;
import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.commons.domain.LanguageCode;

/**
 * Entity to wire shape and back for categories. {@code description} is filled for the language asked for;
 * {@code descriptions} carries every language when {@code allLanguages} is set (the console's private reads).
 */
public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static ReadableCategory toReadable(Category category, LanguageCode language, boolean allLanguages) {
        ReadableCategory readable = new ReadableCategory();
        readable.setId(category.getId());
        readable.setCode(category.getCode());
        readable.setSortOrder(category.getSortOrder());
        readable.setVisible(category.isVisible());
        readable.setFeatured(category.isFeatured());
        readable.setLineage(category.getLineage());
        readable.setDepth(category.getDepth());
        readable.setStore(category.getStoreMerchantId().getId());
        if (category.getParent() != null) {
            readable.setParent(new CategoryReference(category.getParent().getId(), category.getParent().getCode()));
        }
        if (LanguageCode.isLanguage(language)) {
            category.description(language).map(CategoryMapper::description).ifPresent(readable::setDescription);
        }
        if (allLanguages) {
            readable.setDescriptions(category.getDescriptions().stream().map(CategoryMapper::description).toList());
        }
        return readable;
    }

    /**
     * Copies the editable fields of the body onto the entity: code, flags and every language's copy. Descriptions
     * are merged by language so ids and audit dates survive an edit; a language absent from the body is removed.
     */
    public static void apply(PersistableCategory source, Category target) {
        target.setCode(source.getCode());
        target.setSortOrder(source.getSortOrder());
        target.setVisible(source.isVisible());
        target.setFeatured(source.isFeatured());

        Map<LanguageCode, CategoryDescription> existing = new HashMap<>();
        target.getDescriptions().forEach(d -> existing.put(d.getLanguageCode(), d));
        target.getDescriptions().clear();
        for (com.asrevo.cvhome.catalog.model.category.CategoryDescription d : source.getDescriptions()) {
            CategoryDescription entity = existing.getOrDefault(d.getLanguage(), new CategoryDescription(target));
            entity.setLanguageCode(d.getLanguage());
            entity.setName(d.getName());
            entity.setDescription(d.getDescription());
            entity.setSeUrl(d.getFriendlyUrl());
            entity.setHighlight(d.getHighlights());
            entity.setMetaTitle(d.getTitle());
            entity.setMetaKeywords(d.getKeyWords());
            entity.setMetaDescription(d.getMetaDescription());
            target.getDescriptions().add(entity);
        }
    }

    private static com.asrevo.cvhome.catalog.model.category.CategoryDescription description(
            CategoryDescription d) {
        var readable = new com.asrevo.cvhome.catalog.model.category.CategoryDescription();
        readable.setId(d.getId());
        readable.setLanguage(d.getLanguageCode());
        readable.setName(d.getName());
        readable.setDescription(d.getDescription());
        readable.setFriendlyUrl(d.getSeUrl());
        readable.setHighlights(d.getHighlight());
        readable.setTitle(d.getMetaTitle());
        readable.setKeyWords(d.getMetaKeywords());
        readable.setMetaDescription(d.getMetaDescription());
        return readable;
    }
}
