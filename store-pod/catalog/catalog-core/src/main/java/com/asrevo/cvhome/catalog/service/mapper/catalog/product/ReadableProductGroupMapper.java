package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import com.asrevo.cvhome.catalog.entity.product.group.ProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.service.populator.catalog.product.ReadableProductGroupPopulator;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.stereotype.Component;

@Component
public class ReadableProductGroupMapper implements Mapper<ProductGroup, ReadableProductGroup> {

	private final ReadableProductGroupPopulator readableProductGroupPopulator;

	public ReadableProductGroupMapper(ReadableProductGroupPopulator readableProductGroupPopulator) {
		this.readableProductGroupPopulator = readableProductGroupPopulator;
	}

	@Override
	public ReadableProductGroup convert(ProductGroup source, StoreMerchantId store, LanguageCode language) {
		ReadableProductGroup target = new ReadableProductGroup();
		try {
			return readableProductGroupPopulator.populate(source, target, store, language);
		}
		catch (ConversionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ReadableProductGroup merge(ProductGroup source, ReadableProductGroup destination, StoreMerchantId store,
			LanguageCode language) {
		try {
			return readableProductGroupPopulator.populate(source, destination, store, language);
		}
		catch (ConversionException e) {
			throw new RuntimeException(e);
		}
	}

}
