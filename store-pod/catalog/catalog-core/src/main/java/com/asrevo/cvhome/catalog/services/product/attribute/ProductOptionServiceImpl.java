package com.asrevo.cvhome.catalog.services.product.attribute;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.repositories.product.attribute.PageableProductOptionRepository;
import com.asrevo.cvhome.catalog.repositories.product.attribute.ProductOptionRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("productOptionService")
public class ProductOptionServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductOption>
		implements ProductOptionService {

	private final ProductOptionRepository productOptionRepository;

	private final PageableProductOptionRepository pageableProductOptionRepository;

	private final ProductAttributeService productAttributeService;

	@Autowired
	public ProductOptionServiceImpl(ProductOptionRepository productOptionRepository,
			PageableProductOptionRepository pageableProductOptionRepository,
			ProductAttributeService productAttributeService) {
		super(productOptionRepository);
		this.productOptionRepository = productOptionRepository;
		this.pageableProductOptionRepository = pageableProductOptionRepository;
		this.productAttributeService = productAttributeService;
	}

	@Override
	public void saveOrUpdate(ProductOption entity) throws ServiceException {

		// save or update (persist and attach entities
		if (entity.getId() != null && entity.getId() > 0) {
			super.update(entity);
		}
		else {
			super.save(entity);
		}
	}

	@Override
	public void delete(ProductOption entity) throws ServiceException {

		// remove all attributes having this option
		List<ProductAttribute> attributes = productAttributeService.getByOptionId(entity.getStoreMerchantId(),
				entity.getId());

		for (ProductAttribute attribute : attributes) {
			productAttributeService.delete(attribute);
		}

		ProductOption option = this.getById(entity.getId());

		// remove option
		super.delete(option);
	}

	@Override
	public ProductOption getByCode(StoreMerchantId store, String optionCode) {
		return productOptionRepository.findByCode(store, optionCode);
	}

	@Override
	public ProductOption getById(StoreMerchantId store, Long optionId) {
		return productOptionRepository.findOne(store, optionId);
	}

	@Override
	public Page<ProductOption> getByMerchant(StoreMerchantId store, LanguageCode language, String name,
			Pageable pageable) {
		Assert.notNull(store, "Store cannot be null");
		return pageableProductOptionRepository.listOptions(store, name, pageable);
	}

}
