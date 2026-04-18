package com.asrevo.cvhome.catalog.service.populator.catalog;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.model.category.CategoryDescription;
import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
public class PersistableCategoryPopulator
        extends AbstractDataPopulator<PersistableCategory, StoreMerchantId, Category> {

    private final CategoryService categoryService;

    public PersistableCategoryPopulator(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public Category populate(PersistableCategory source, Category target, StoreMerchantId store, LanguageCode language)
            throws ConversionException {

        try {

            Assert.notNull(target, "Category target cannot be null");

            /*
             * Assert.notNull(categoryService, "Requires to set CategoryService");
             * Assert.notNull(languageService, "Requires to set LanguageService");
             */

            target.setStoreMerchantId(store);
            target.setCode(source.getCode());
            target.setSortOrder(source.getSortOrder());
            target.setVisible(source.isVisible());
            target.setFeatured(source.isFeatured());

            // children
            if (CollectionUtils.isEmpty(source.getChildren())) {
                target.getCategories().clear();
            }
            // no modifications to children category

            // get parent

            if (source.getParent() == null || (StringUtils.isBlank(source.getParent().getCode()))
                    || source.getParent().getId() == null) {
                target.setParent(null);
                target.setDepth(0);
                target.setLineage(new StringBuilder().append("/").append(source.getId()).append("/").toString());
            } else {
                Category parent;
                if (!StringUtils.isBlank(source.getParent().getCode())) {
                    parent = categoryService.getByCode(store, source.getParent().getCode());
                } else if (source.getParent().getId() != null) {
                    parent = categoryService.getById(source.getParent().getId(), store);
                } else {
                    throw new ConversionException("Category parent needs at least an id or a code for reference");
                }
                if (parent != null && !Objects.equals(parent.getStoreMerchantId(), store)) {
                    throw new ConversionException("Store id does not belong to specified parent id");
                }

                if (parent != null) {
                    target.setParent(parent);

                    String lineage = parent.getLineage();
                    int depth = parent.getDepth();

                    target.setDepth(depth + 1);
                    target
                            .setLineage(new StringBuilder().append(lineage).append(target.getId()).append("/").toString());
                }
            }

            if (!CollectionUtils.isEmpty(source.getChildren())) {

                for (PersistableCategory cat : source.getChildren()) {

                    Category persistCategory = this.populate(cat, new Category(), store, language);
                    target.getCategories().add(persistCategory);
                }
            }

            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                Set<com.asrevo.cvhome.catalog.entity.category.CategoryDescription> descriptions = new HashSet<>();
                if (CollectionUtils.isNotEmpty(target.getDescriptions())) {
                    for (com.asrevo.cvhome.catalog.entity.category.CategoryDescription description : target
                            .getDescriptions()) {
                        for (CategoryDescription d : source.getDescriptions()) {
                            if (StringUtils.isBlank(d.getLanguage().code())) {
                                throw new ConversionException("Source category description has no language");
                            }
                            if (d.getLanguage().equals(description.getLanguageCode())) {
                                description.setCategory(target);
                                description = buildDescription(d, description);
                                descriptions.add(description);
                            }
                        }
                    }

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

            return target;

        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    private com.asrevo.cvhome.catalog.entity.category.CategoryDescription buildDescription(CategoryDescription source,
                                                                                           com.asrevo.cvhome.catalog.entity.category.CategoryDescription target) {
        // com.salesmanager.core.model.catalog.category.CategoryDescription desc = new
        // com.salesmanager.core.model.catalog.category.CategoryDescription();
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
        // TODO Auto-generated method stub
        return null;
    }

}
