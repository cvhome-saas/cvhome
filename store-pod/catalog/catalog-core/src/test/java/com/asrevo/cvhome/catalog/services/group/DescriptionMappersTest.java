package com.asrevo.cvhome.catalog.services.group;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.CategoryDescription;
import com.asrevo.cvhome.catalog.entity.ProductGroup;
import com.asrevo.cvhome.catalog.entity.ProductGroupDescription;
import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.model.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.services.category.CategoryMapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How a category's and a group's per-language copy is replaced on save.
 *
 * <p>
 * Both mappers clear the description set and rebuild it, reusing the existing row for a language they already
 * had. The reuse is the point: replacing the row would orphan the old one and, with {@code orphanRemoval}, delete
 * and re-insert it on every save — churning ids the console holds and, for a category, the {@code seUrl} that a
 * shopper's bookmark points at. A language dropped from the request is meant to disappear and one added is meant
 * to arrive, so both directions are asserted for each.
 * </p>
 */
class DescriptionMappersTest {

    private static final LanguageCode ENGLISH = new LanguageCode("en");
    private static final LanguageCode ARABIC = new LanguageCode("ar");
    private static final String NAME = "Shoes";
    private static final String SLUG = "shoes";
    private static final String CODE = SLUG;
    private static final String TITLE = "t";
    private static final String KEYWORDS = "k";
    private static final String META = "m";
    private static final String HIGHLIGHT = "h";

    private static Category categoryWith(LanguageCode language) {
        Category category = new Category();
        category.setId(1L);
        CategoryDescription description = new CategoryDescription(category);
        description.setLanguageCode(language);
        description.setName(NAME);
        category.getDescriptions().add(description);
        return category;
    }

    private static PersistableCategory persistableCategory(LanguageCode... languages) {
        PersistableCategory source = new PersistableCategory();
        source.setCode(CODE);
        source.setDescriptions(List.of(languages).stream().map(language -> {
            var description = new com.asrevo.cvhome.catalog.model.category.CategoryDescription();
            description.setLanguage(language);
            description.setName(NAME);
            description.setFriendlyUrl(SLUG);
            description.setTitle(TITLE);
            description.setKeyWords(KEYWORDS);
            description.setMetaDescription(META);
            description.setHighlights(HIGHLIGHT);
            return description;
        }).toList());
        return source;
    }

    private static ProductGroup groupWith(LanguageCode language) {
        ProductGroup group = new ProductGroup();
        group.setId(1L);
        ProductGroupDescription description = new ProductGroupDescription(group);
        description.setLanguageCode(language);
        description.setName(NAME);
        group.getDescriptions().add(description);
        return group;
    }

    private static PersistableProductGroup persistableGroup(LanguageCode... languages) {
        PersistableProductGroup source = new PersistableProductGroup();
        source.setCode(CODE);
        source.setDescriptions(List.of(languages).stream().map(language -> {
            var description = new com.asrevo.cvhome.catalog.model.group.ProductGroupDescription();
            description.setLanguage(language);
            description.setName(NAME);
            description.setFriendlyUrl(SLUG);
            description.setTitle(TITLE);
            description.setKeyWords(KEYWORDS);
            description.setMetaDescription(META);
            return description;
        }).toList());
        return source;
    }

    @Test
    void aCategorysExistingLanguageRowIsReusedRatherThanReplaced() {
        Category category = categoryWith(ENGLISH);
        CategoryDescription before = category.getDescriptions().iterator().next();

        CategoryMapper.apply(persistableCategory(ENGLISH), category);

        assertThat(category.getDescriptions()).hasSize(1).containsExactly(before);
        assertThat(before.getSeUrl()).isEqualTo(SLUG);
        assertThat(before.getMetaTitle()).isEqualTo(TITLE);
    }

    @Test
    void aCategoryLanguageAddedArrivesAndOneDroppedDisappears() {
        Category category = categoryWith(ENGLISH);

        CategoryMapper.apply(persistableCategory(ENGLISH, ARABIC), category);
        assertThat(category.getDescriptions()).hasSize(2);

        CategoryMapper.apply(persistableCategory(ARABIC), category);
        assertThat(category.getDescriptions()).hasSize(1)
                .allSatisfy(d -> assertThat(d.getLanguageCode()).isEqualTo(ARABIC));
    }

    @Test
    void theCategoryFlagsAreCopiedStraightAcross() {
        Category category = categoryWith(ENGLISH);
        PersistableCategory source = persistableCategory(ENGLISH);
        source.setVisible(true);
        source.setFeatured(true);
        source.setSortOrder(3);

        CategoryMapper.apply(source, category);

        assertThat(category.isVisible()).isTrue();
        assertThat(category.isFeatured()).isTrue();
        assertThat(category.getSortOrder()).isEqualTo(3);
        assertThat(category.getCode()).isEqualTo(CODE);
    }

    @Test
    void aGroupsExistingLanguageRowIsReusedRatherThanReplaced() {
        ProductGroup group = groupWith(ENGLISH);
        ProductGroupDescription before = group.getDescriptions().iterator().next();

        ProductGroupMapper.apply(persistableGroup(ENGLISH), group);

        assertThat(group.getDescriptions()).hasSize(1).containsExactly(before);
        assertThat(before.getSeUrl()).isEqualTo(SLUG);
    }

    @Test
    void aGroupLanguageAddedArrivesAndOneDroppedDisappears() {
        ProductGroup group = groupWith(ENGLISH);

        ProductGroupMapper.apply(persistableGroup(ENGLISH, ARABIC), group);
        assertThat(group.getDescriptions()).hasSize(2);

        ProductGroupMapper.apply(persistableGroup(ARABIC), group);
        assertThat(group.getDescriptions()).hasSize(1)
                .allSatisfy(d -> assertThat(d.getLanguageCode()).isEqualTo(ARABIC));
    }
}
