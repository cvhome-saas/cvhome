package com.asrevo.cvhome.catalog.services.product.group;

import com.asrevo.cvhome.catalog.entity.product.group.ProductGroup;
import com.asrevo.cvhome.catalog.repositories.product.group.ProductGroupRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("productGroupService")
public class ProductGroupServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductGroup>
		implements ProductGroupService {

	private final ProductGroupRepository productGroupRepository;

	public ProductGroupServiceImpl(ProductGroupRepository productGroupRepository) {
		super(productGroupRepository);
		this.productGroupRepository = productGroupRepository;
	}

	@Override
	@Transactional
	public void saveOrUpdate(ProductGroup productGroup) throws ServiceException {
		productGroupRepository.save(productGroup);
	}

	@Override
	public Optional<ProductGroup> getByCode(StoreMerchantId store, String code) throws ServiceException {
		return productGroupRepository.findByStoreAndCode(store, code);
	}

	@Override
	public Page<ProductGroup> listByStore(StoreMerchantId store, LanguageCode language, Pageable pageable) {
		return productGroupRepository.findByStore(store, language, pageable);
	}

	@Override
	@Transactional
	public void delete(ProductGroup productGroup) throws ServiceException {
		productGroupRepository.delete(productGroup);
	}

	@Override
	public List<ProductGroup> listByParentProduct(StoreMerchantId store, Long productId) throws ServiceException {
		return productGroupRepository.findByStoreAndParentProduct(store, productId);
	}

	@Override
	public Optional<ProductGroup> getByParentProductAndCode(StoreMerchantId store, Long productId, String code)
			throws ServiceException {
		return productGroupRepository.findByStoreAndParentProductAndCode(store, productId, code);
	}

}
