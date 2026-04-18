package com.asrevo.cvhome.catalog.service.populator.catalog.product;

import com.asrevo.cvhome.catalog.entity.product.group.ProductGroup;
import com.asrevo.cvhome.catalog.entity.product.group.ProductGroupDescription;
import com.asrevo.cvhome.catalog.model.product.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.PersistableProductGroupDescription;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class PersistableProductGroupPopulator
		extends AbstractDataPopulator<PersistableProductGroup, StoreMerchantId, ProductGroup> {

	private final ProductService productService;

	public PersistableProductGroupPopulator(ProductService productService) {
		this.productService = productService;
	}

	@Override
	public ProductGroup populate(PersistableProductGroup source, ProductGroup target, StoreMerchantId store,
			LanguageCode language) throws ConversionException {
		target.setStoreMerchantId(store);
		target.setCode(source.getCode());
		target.setActive(source.isActive());

		if (source.getParentProductId() != null) {
			target.setParentProduct(productService.getById(source.getParentProductId()));
		}

		if (CollectionUtils.isNotEmpty(source.getProductIds())) {
			target
				.setProducts(source.getProductIds().stream().map(productService::getById).collect(Collectors.toList()));
		}

		if (CollectionUtils.isNotEmpty(source.getDescriptions())) {
			Set<ProductGroupDescription> descriptions = new HashSet<>();
			for (PersistableProductGroupDescription d : source.getDescriptions()) {
				ProductGroupDescription desc = target.getDescriptions()
					.stream()
					.filter(td -> td.getLanguageCode().equals(d.getLanguage()))
					.findFirst()
					.orElse(new ProductGroupDescription());

				desc.setProductGroup(target);
				desc.setLanguageCode(d.getLanguage());
				desc.setName(d.getName());
				desc.setDescription(d.getDescription());
				desc.setSeUrl(d.getFriendlyUrl());
				desc.setMetatagTitle(d.getTitle());
				desc.setMetatagDescription(d.getMetaDescription());
				desc.setMetatagKeywords(d.getKeyWords());
				descriptions.add(desc);
			}
			target.setDescriptions(descriptions);
		}

		return target;
	}

	@Override
	protected ProductGroup createTarget() {
		return new ProductGroup();
	}

}
