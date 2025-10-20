package com.asrevo.cvhome.catalog.service.mapper.catalog;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.category.CategoryDescription;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReadableCategoryMapper implements Mapper<Category, ReadableCategory> {

	@Override
	public ReadableCategory convert(Category source, StoreMerchantId store, LanguageCode language) {
		ReadableCategory target = new ReadableCategory();

		target.setId(source.getId());
		target.setCode(source.getCode());
		target.setLineage(source.getLineage());
		target.setStore(source.getStoreMerchantId().getId());
		target.setSortOrder(source.getSortOrder());
		target.setVisible(source.isVisible());
		target.setFeatured(source.isFeatured());
		target.setDepth(source.getDepth());
		target.setParent(createParentCategory(source, language).orElse(null));

		if (LanguageCode.isAllLanguage(language)) {
			var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(Set.of());
			target.setDescriptions(descriptionSet.stream().map(this::convertDescription).toList());
		}
		if (LanguageCode.isLanguage(language)) {
			var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(Set.of());
			var description = descriptionSet.stream()
				.filter(it -> language.equals(it.getLanguageCode()))
				.findFirst()
				.map(this::convertDescription)
				.orElse(null);
			target.setDescription(description);
		}
		return target;
	}

	private com.asrevo.cvhome.catalog.model.category.CategoryDescription convertDescription(
			CategoryDescription description) {
		var d = new com.asrevo.cvhome.catalog.model.category.CategoryDescription();
		d.setFriendlyUrl(description.getSeUrl());
		d.setName(description.getName());
		d.setId(description.getId());
		d.setDescription(description.getDescription());
		d.setKeyWords(description.getMetatagKeywords());
		d.setHighlights(description.getCategoryHighlight());
		d.setLanguage(description.getLanguageCode());
		d.setTitle(description.getMetatagTitle());
		d.setMetaDescription(description.getMetatagDescription());
		return d;
	}

	private Optional<com.asrevo.cvhome.catalog.model.category.Category> createParentCategory(Category source,
			LanguageCode language) {

		return Optional.ofNullable(source.getParent()).map(p -> {
			var parent = new com.asrevo.cvhome.catalog.model.category.Category();
			parent.setCode(p.getCode());
			parent.setId(p.getId());
			return parent;
		});
	}

	@Override
	public ReadableCategory merge(Category source, ReadableCategory destination, StoreMerchantId store,
			LanguageCode language) {
		return destination;
	}

}
