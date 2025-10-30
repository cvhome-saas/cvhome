package com.asrevo.cvhome.catalog.service.mapper.catalog;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.model.product.ReadableProductAvailability;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.DateUtil;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ReadableProductAvailabilityMapper implements Mapper<Product, ReadableProductAvailability> {

	@Override
	public ReadableProductAvailability convert(Product source, StoreMerchantId store, LanguageCode language) {
		return this.merge(source, new ReadableProductAvailability(), store, language);
	}

	@Override
	public ReadableProductAvailability merge(Product source, ReadableProductAvailability destination,
			StoreMerchantId store, LanguageCode language) {
		Assert.notNull(source, "Product must not be null");
		Assert.notNull(destination, "ReadableProductAvailability must not be null");
		Assert.notNull(store, "StoreMerchantId must not be null");
		destination.setSku(source.getSku());
		destination.setStore(store);
		for (ProductAvailability availability : source.getAvailabilities()) {

			destination.setQuantity(Optional.ofNullable(availability.getProductQuantity()).orElse(1));
			destination
				.setQuantityOrderMaximum(Optional.ofNullable(availability.getProductQuantityOrderMax()).orElse(1));
			destination
				.setQuantityOrderMinimum(Optional.ofNullable(availability.getProductQuantityOrderMin()).orElse(1));
			if (availability.getProductQuantity() > 0 && source.isAvailable()) {
				destination.setCanBePurchased(true);
			}
			if (source.getDateAvailable() != null) {
				destination.setDateAvailable(DateUtil.formatDate(source.getDateAvailable()));
			}

			if (availability.getProductVariant() == null && StringUtils.isEmpty(availability.getRegionVariant())) {
				break;
			}
		}

		return destination;
	}

}
