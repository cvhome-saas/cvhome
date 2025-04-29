package com.asrevo.cvhome.catalog.service.mapper.catalog;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.category.CategoryDescription;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategoryFull;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReadableCategoryMapper implements Mapper<Category, ReadableCategory> {

    @Override
    public ReadableCategory convert(Category source, StoreMerchantId store, LanguageCode language) {

        if (Objects.isNull(language)) {
            ReadableCategoryFull target = new ReadableCategoryFull();
            List<com.asrevo.cvhome.catalog.model.category.CategoryDescription> descriptions =
                    source.getDescriptions().stream()
                            .map(this::convertDescription)
                            .collect(Collectors.toList());
            target.setDescriptions(descriptions);
            fillReadableCategory(target, source, language);
            return target;
        } else {
            // fillReadableCategory(target, source, language);
            return createReadable(source, language);
        }
    }

    private void fillReadableCategory(
            ReadableCategory target, Category source, LanguageCode language) {
        Optional<com.asrevo.cvhome.catalog.model.category.Category> parentCategory =
                createParentCategory(source, language);
        parentCategory.ifPresent(target::setParent);

        Optional.ofNullable(source.getDepth()).ifPresent(target::setDepth);

        target.setLineage(source.getLineage());
        target.setStore(source.getStoreMerchantId().getId());
        target.setCode(source.getCode());
        target.setId(source.getId());
        target.setSortOrder(source.getSortOrder());
        target.setVisible(source.isVisible());
        target.setFeatured(source.isFeatured());
    }

    private com.asrevo.cvhome.catalog.model.category.CategoryDescription convertDescription(
            CategoryDescription description) {
        final com.asrevo.cvhome.catalog.model.category.CategoryDescription desc =
                new com.asrevo.cvhome.catalog.model.category.CategoryDescription();

        desc.setFriendlyUrl(description.getSeUrl());
        desc.setName(description.getName());
        desc.setId(description.getId());
        desc.setDescription(description.getDescription());
        desc.setKeyWords(description.getMetatagKeywords());
        desc.setHighlights(description.getCategoryHighlight());
        desc.setLanguage(description.getLanguageCode());
        desc.setTitle(description.getMetatagTitle());
        desc.setMetaDescription(description.getMetatagDescription());
        return desc;
    }

    private Optional<com.asrevo.cvhome.catalog.model.category.Category> createParentCategory(
            Category source, LanguageCode language) {

        return Optional.ofNullable(source.getParent())
                .map(
                        parentValue -> {
                            final com.asrevo.cvhome.catalog.model.category.Category parent =
                                    new com.asrevo.cvhome.catalog.model.category.Category();

                            Optional<com.asrevo.cvhome.catalog.model.category.CategoryDescription>
                                    description =
                                            source.getDescriptions().stream()
                                                    .filter(
                                                            d ->
                                                                    Objects.isNull(language)
                                                                            || language.equals(
                                                                                    d
                                                                                            .getLanguageCode()))
                                                    .map(this::convertDescription)
                                                    .findAny();

                            parent.setCode(source.getParent().getCode());
                            parent.setId(source.getParent().getId());
                            description.ifPresent(parent::setDescription);
                            return parent;
                        });
    }

    @Override
    public ReadableCategory merge(
            Category source,
            ReadableCategory destination,
            StoreMerchantId store,
            LanguageCode language) {
        return destination;
    }

    private ReadableCategory createReadable(Category category, LanguageCode language) {

        ReadableCategory current = new ReadableCategory();
        this.fillReadableCategory(current, category, language);
        Optional<com.asrevo.cvhome.catalog.model.category.CategoryDescription> description =
                category.getDescriptions().stream()
                        .filter(d -> language.equals(d.getLanguageCode()))
                        .map(this::convertDescription)
                        .findAny();

        description.ifPresent(current::setDescription);

        if (category.getParent() != null) {
            current.setParent(this.createReadable(category.getParent(), language));
        }

        return current;
    }
}
