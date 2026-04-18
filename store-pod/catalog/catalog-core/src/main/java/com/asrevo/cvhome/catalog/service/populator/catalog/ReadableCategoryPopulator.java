package com.asrevo.cvhome.catalog.service.populator.catalog;

import org.springframework.util.Assert;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.category.CategoryDescription;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

public class ReadableCategoryPopulator extends AbstractDataPopulator<Category, StoreMerchantId, ReadableCategory> {

    @Override
    public ReadableCategory populate(final Category source, final ReadableCategory target, final StoreMerchantId store,
                                     final LanguageCode language) {

        Assert.notNull(source, "Category must not be null");

        target.setLineage(source.getLineage());
        if (source.getDescriptions() != null && !source.getDescriptions().isEmpty()) {

            CategoryDescription description = source.getDescription();
            if (source.getDescriptions().size() > 1) {
                for (final CategoryDescription desc : source.getDescriptions()) {
                    if (desc.getLanguageCode().equals(language)) {
                        description = desc;
                        break;
                    }
                }
            }

            if (description != null) {
                final com.asrevo.cvhome.catalog.model.category.CategoryDescription desc =
                        new com.asrevo.cvhome.catalog.model.category.CategoryDescription();
                desc.setFriendlyUrl(description.getSeUrl());
                desc.setName(description.getName());
                desc.setId(source.getId());
                desc.setDescription(description.getDescription());
                desc.setKeyWords(description.getMetatagKeywords());
                desc.setHighlights(description.getCategoryHighlight());
                desc.setTitle(description.getMetatagTitle());
                desc.setMetaDescription(description.getMetatagDescription());
                desc.setLanguage(description.getLanguageCode());

                target.setDescription(desc);
            }
        }

        if (source.getParent() != null) {
            final com.asrevo.cvhome.catalog.model.category.Category parent = new com.asrevo.cvhome.catalog.model.category.Category();
            parent.setCode(source.getParent().getCode());
            parent.setId(source.getParent().getId());
            target.setParent(parent);
        }

        target.setCode(source.getCode());
        target.setId(source.getId());
        if (source.getDepth() != null) {
            target.setDepth(source.getDepth());
        }
        target.setSortOrder(source.getSortOrder());
        target.setVisible(source.isVisible());
        target.setFeatured(source.isFeatured());

        return target;
    }

    @Override
    protected ReadableCategory createTarget() {
        return null;
    }

}
