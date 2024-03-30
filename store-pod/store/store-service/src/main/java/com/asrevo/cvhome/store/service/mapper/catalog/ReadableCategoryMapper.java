package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.category.CategoryDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategoryFull;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ReadableCategoryMapper implements Mapper<Category, ReadableCategory> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadableCategoryMapper.class);

    @Override
    public ReadableCategory convert(Category source, MerchantStore store, Language language) {

        if (Objects.isNull(language)) {
            ReadableCategoryFull target = new ReadableCategoryFull();
            List<com.asrevo.cvhome.store.core.model.catalog.category.CategoryDescription> descriptions = source
                    .getDescriptions().stream().map(this::convertDescription).collect(Collectors.toList());
            target.setDescriptions(descriptions);
            fillReadableCategory(target, source, language);
            return target;
        } else {
            /**
             ReadableCategory target = new ReadableCategory();
             Optional<com.asrevo.cvhome.store.model.catalog.category.CategoryDescription> description = source
             .getDescriptions().stream().filter(d -> language.getId().equals(d.getLanguage().getId()))
             .map(this::convertDescription).findAny();
             description.ifPresent(target::setDescript;ion);
             **/
            ReadableCategory target = createReadable(source, language);
            //fillReadableCategory(target, source, language);
            return target;
        }
    }

    private void fillReadableCategory(ReadableCategory target, Category source, Language language) {
        Optional<com.asrevo.cvhome.store.core.model.catalog.category.Category> parentCategory = createParentCategory(source,
                language);
        parentCategory.ifPresent(target::setParent);

        Optional.ofNullable(source.getDepth()).ifPresent(target::setDepth);

        target.setLineage(source.getLineage());
        target.setStore(source.getMerchantStore().getCode());
        target.setCode(source.getCode());
        target.setId(source.getId());
        target.setSortOrder(source.getSortOrder());
        target.setVisible(source.isVisible());
        target.setFeatured(source.isFeatured());
    }

    private com.asrevo.cvhome.store.core.model.catalog.category.CategoryDescription convertDescription(
            CategoryDescription description) {
        final com.asrevo.cvhome.store.core.model.catalog.category.CategoryDescription desc = new com.asrevo.cvhome.store.core.model.catalog.category.CategoryDescription();

        desc.setFriendlyUrl(description.getSeUrl());
        desc.setName(description.getName());
        desc.setId(description.getId());
        desc.setDescription(description.getDescription());
        desc.setKeyWords(description.getMetatagKeywords());
        desc.setHighlights(description.getCategoryHighlight());
        desc.setLanguage(description.getLanguage().getCode());
        desc.setTitle(description.getMetatagTitle());
        desc.setMetaDescription(description.getMetatagDescription());
        return desc;
    }

    private Optional<com.asrevo.cvhome.store.core.model.catalog.category.Category> createParentCategory(Category source,
                                                                                                        Language language) {

        return Optional.ofNullable(source.getParent()).map(parentValue -> {
            final com.asrevo.cvhome.store.core.model.catalog.category.Category parent = new com.asrevo.cvhome.store.core.model.catalog.category.Category();

            if (source.getParent() != null) {

            }
            Optional<com.asrevo.cvhome.store.core.model.catalog.category.CategoryDescription> description = source
                    .getDescriptions().stream().filter(d -> language.getId().equals(d.getLanguage().getId()))
                    .map(this::convertDescription).findAny();

            parent.setCode(source.getParent().getCode());
            parent.setId(source.getParent().getId());
            if (description.isPresent()) {
                parent.setDescription(description.get());
            }
            return parent;
        });
    }

    @Override
    public ReadableCategory merge(Category source, ReadableCategory destination, MerchantStore store,
                                  Language language) {
        return destination;
    }

    private ReadableCategory createReadable(Category category, Language language) {

        ReadableCategory current = new ReadableCategory();
        this.fillReadableCategory(current, category, language);
        Optional<com.asrevo.cvhome.store.core.model.catalog.category.CategoryDescription> description = category
                .getDescriptions().stream().filter(d -> language.getId().equals(d.getLanguage().getId()))
                .map(this::convertDescription).findAny();

        if (description.isPresent()) {
            current.setDescription(description.get());
        }

        if (category.getParent() != null) {
            current.setParent(this.createReadable(category.getParent(), language));
        }

        return current;

    }

}
