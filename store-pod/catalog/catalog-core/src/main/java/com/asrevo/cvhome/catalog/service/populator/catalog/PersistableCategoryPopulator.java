package com.asrevo.cvhome.catalog.service.populator.catalog;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.errors.CategoryDescriptionLanguageMissingException;
import com.asrevo.cvhome.catalog.errors.CategoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.category.CategoryDescription;
import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
public class PersistableCategoryPopulator
        extends AbstractDataPopulator<PersistableCategory, StoreMerchantId, Category> {

    private static final String PATH_SEPARATOR = "/";

    private final CategoryService categoryService;

    public PersistableCategoryPopulator(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public Category populate(PersistableCategory source, Category target, StoreMerchantId store, LanguageCode language)
            throws CategoryNotConvertibleException, CategoryReferenceUnresolvableException,
            CategoryDescriptionLanguageMissingException {

        try {

            target.setStoreMerchantId(store);
            target.setCode(source.getCode());
            target.setSortOrder(source.getSortOrder());
            target.setVisible(source.isVisible());
            target.setFeatured(source.isFeatured());

            if (CollectionUtils.isEmpty(source.getChildren())) {
                target.getCategories().clear();
            }

            applyParent(source, target, store);
            applyChildren(source, target, store, language);
            applyDescriptions(source, target);

            return target;

        } catch (Exception e) {
            throw CategoryNotConvertibleException.of(e);
        }
    }

    private void applyParent(PersistableCategory source, Category target, StoreMerchantId store)
            throws CategoryReferenceUnresolvableException, ServiceException {
        if (source.getParent() == null || StringUtils.isBlank(source.getParent().getCode())
                || source.getParent().getId() == null) {
            target.setParent(null);
            target.setDepth(0);
            target.setLineage(
                    new StringBuilder().append(PATH_SEPARATOR).append(source.getId()).append(PATH_SEPARATOR).toString());
            return;
        }

        Category parent = resolveParent(source, store);
        if (parent != null && !Objects.equals(parent.getStoreMerchantId(), store)) {
            // Reported as unresolvable rather than as a cross-store rule: saying "that parent belongs to another
            // store" would confirm the row exists, which a caller probing category ids should not learn.
            throw CategoryReferenceUnresolvableException.of(parent.getId(), store);
        }

        if (parent != null) {
            target.setParent(parent);

            String lineage = parent.getLineage();
            int depth = parent.getDepth();

            target.setDepth(depth + 1);
            target.setLineage(
                    new StringBuilder().append(lineage).append(target.getId()).append(PATH_SEPARATOR).toString());
        }
    }

    private Category resolveParent(PersistableCategory source, StoreMerchantId store)
            throws CategoryReferenceUnresolvableException, ServiceException {
        if (!StringUtils.isBlank(source.getParent().getCode())) {
            return categoryService.getByCode(store, source.getParent().getCode());
        }
        if (source.getParent().getId() != null) {
            return categoryService.getById(source.getParent().getId(), store);
        }
        throw CategoryReferenceUnresolvableException.incomplete();
    }

    private void applyChildren(PersistableCategory source, Category target, StoreMerchantId store, LanguageCode language)
            throws CategoryNotConvertibleException, CategoryReferenceUnresolvableException,
            CategoryDescriptionLanguageMissingException, ServiceException {
        if (CollectionUtils.isEmpty(source.getChildren())) {
            return;
        }
        for (PersistableCategory cat : source.getChildren()) {
            Category persistCategory = this.populate(cat, new Category(), store, language);
            target.getCategories().add(persistCategory);
        }
    }

    private void applyDescriptions(PersistableCategory source, Category target)
            throws CategoryDescriptionLanguageMissingException {
        if (CollectionUtils.isEmpty(source.getDescriptions())) {
            return;
        }
        Set<com.asrevo.cvhome.catalog.entity.category.CategoryDescription> descriptions = new HashSet<>();
        if (CollectionUtils.isNotEmpty(target.getDescriptions())) {
            mergeExistingDescriptions(source, target, descriptions);
        } else {
            for (CategoryDescription d : source.getDescriptions()) {
                com.asrevo.cvhome.catalog.entity.category.CategoryDescription t =
                        new com.asrevo.cvhome.catalog.entity.category.CategoryDescription();

                this.buildDescription(d, t);
                t.setCategory(target);
                descriptions.add(t);
            }
        }
        target.setDescriptions(descriptions);
    }

    private void mergeExistingDescriptions(PersistableCategory source, Category target,
                                           Set<com.asrevo.cvhome.catalog.entity.category.CategoryDescription> descriptions)
            throws CategoryDescriptionLanguageMissingException {
        for (com.asrevo.cvhome.catalog.entity.category.CategoryDescription description : target.getDescriptions()) {
            for (CategoryDescription d : source.getDescriptions()) {
                if (StringUtils.isBlank(d.getLanguage().code())) {
                    throw CategoryDescriptionLanguageMissingException.of();
                }
                if (d.getLanguage().equals(description.getLanguageCode())) {
                    description.setCategory(target);
                    com.asrevo.cvhome.catalog.entity.category.CategoryDescription merged =
                            buildDescription(d, description);
                    descriptions.add(merged);
                }
            }
        }
    }

    private com.asrevo.cvhome.catalog.entity.category.CategoryDescription buildDescription(
            CategoryDescription source,
            com.asrevo.cvhome.catalog.entity.category.CategoryDescription target) {
        target.setCategoryHighlight(source.getHighlights());
        target.setDescription(source.getDescription());
        target.setName(source.getName());
        target.setMetatagDescription(source.getMetaDescription());
        target.setMetatagTitle(source.getTitle());
        target.setSeUrl(source.getFriendlyUrl());
        target.setLanguageCode(source.getLanguage());
        return target;
    }

    @Override
    protected Category createTarget() {

        return null;
    }

}
