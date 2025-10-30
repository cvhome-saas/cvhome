package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.entity.product.variation.ProductVariation;
import com.asrevo.cvhome.catalog.model.product.variation.PersistableProductVariation;
import com.asrevo.cvhome.catalog.model.product.variation.ReadableProductVariation;
import com.asrevo.cvhome.catalog.service.mapper.catalog.PersistableProductVariationMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductVariationMapper;
import com.asrevo.cvhome.catalog.services.product.variation.ProductVariationService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class ProductVariationFacadeImpl implements ProductVariationFacade {

	private final PersistableProductVariationMapper persistableProductVariationMapper;

	private final ReadableProductVariationMapper readableProductVariationMapper;

	private final ProductVariationService productVariationService;

	public ProductVariationFacadeImpl(PersistableProductVariationMapper persistableProductVariationMapper,
			ReadableProductVariationMapper readableProductVariationMapper,
			ProductVariationService productVariationService) {
		this.persistableProductVariationMapper = persistableProductVariationMapper;
		this.readableProductVariationMapper = readableProductVariationMapper;
		this.productVariationService = productVariationService;
	}

	@Override
	public ReadableProductVariation get(Long variationId, StoreMerchantId store, LanguageCode language) {
		Assert.notNull(store, "store cannot be null");
		Assert.notNull(language, "LanguageCode cannot be null");
		Optional<ProductVariation> variation = productVariationService.getById(store, variationId, language);
		if (variation.isEmpty()) {
			throw new ResourceNotFoundException(
					"ProductVariation not found for id [" + variationId + "] and store [" + store + "]");
		}

		return readableProductVariationMapper.convert(variation.get(), store, language);
	}

	@Override
	public ReadableEntityList<ReadableProductVariation> list(StoreMerchantId store, LanguageCode language,
			Pageable pageable) {
		Assert.notNull(store, "store cannot be null");
		Assert.notNull(language, "LanguageCode cannot be null");

		Page<ProductVariation> vars = productVariationService.getByMerchant(store, language, null, pageable);
		List<ReadableProductVariation> variations = vars.stream()
			.map(opt -> this.convert(opt, store, language))
			.collect(Collectors.toList());
		ReadableEntityList<ReadableProductVariation> returnList = new ReadableEntityList<>();
		returnList.setContent(variations);
		returnList.setSize(variations.size());
		returnList.setTotalElements(vars.getTotalElements());
		returnList.setTotalPages(vars.getTotalPages());
		return returnList;
	}

	private ReadableProductVariation convert(ProductVariation var, StoreMerchantId store, LanguageCode language) {
		return readableProductVariationMapper.convert(var, store, language);
	}

	@Override
	public Long create(PersistableProductVariation var, StoreMerchantId store, LanguageCode language) {
		Assert.notNull(store, "store cannot be null");
		Assert.notNull(language, "LanguageCode cannot be null");
		Assert.notNull(var, "PersistableProductVariation cannot be null");

		if (this.exists(var.getCode(), store)) {
			throw new OperationNotAllowedException("Option set with code [" + var.getCode() + "] already exist");
		}

		ProductVariation p = persistableProductVariationMapper.convert(var, store, language);
		p.setStoreMerchantId(store);
		try {
			productVariationService.saveOrUpdate(p);
		}
		catch (ServiceException e) {
			throw new ServiceRuntimeException("Exception while creating ProductOptionSet", e);
		}

		return p.getId();
	}

	@Override
	public void update(Long variationId, PersistableProductVariation var, StoreMerchantId store,
			LanguageCode language) {
		Assert.notNull(store, "store cannot be null");
		Assert.notNull(language, "Language cannot be null");
		Assert.notNull(var, "PersistableProductVariation cannot be null");

		Optional<ProductVariation> p = productVariationService.getById(store, variationId, language);
		if (p.isEmpty()) {
			throw new ResourceNotFoundException(
					"ProductVariation not found for id [" + variationId + "] and store [" + store + "]");
		}

		ProductVariation productVariant = p.get();

		productVariant.setId(variationId);
		productVariant.setCode(var.getCode());
		ProductVariation model = persistableProductVariationMapper.merge(var, productVariant, store, language);
		model.setStoreMerchantId(store);
		productVariationService.save(model);
	}

	@Override
	public void delete(Long variationId, StoreMerchantId store) {
		Assert.notNull(store, "store cannot be null");
		Assert.notNull(variationId, "variationId cannot be null");
		ProductVariation opt = productVariationService.getById(variationId);
		if (opt == null) {
			throw new ResourceNotFoundException(
					"ProductVariation not found for id [" + variationId + "] and store [" + store + "]");
		}
		if (!opt.getStoreMerchantId().equals(store)) {
			throw new ResourceNotFoundException(
					"ProductVariation not found for id [" + variationId + "] and store [" + store + "]");
		}
		try {
			productVariationService.delete(opt);
		}
		catch (ServiceException e) {
			throw new ServiceRuntimeException("Exception while deleting ProductVariation", e);
		}
	}

	@Override
	public boolean exists(String code, StoreMerchantId store) {
		Assert.notNull(store, "store cannot be null");
		Assert.notNull(code, "code cannot be null");
		Optional<ProductVariation> var = productVariationService.getByCode(store, code);
		return var.isPresent();
	}

}
