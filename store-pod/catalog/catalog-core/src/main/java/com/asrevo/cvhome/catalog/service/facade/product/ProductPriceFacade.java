package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.model.product.PersistableProductPrice;
import com.asrevo.cvhome.catalog.model.product.ReadableProductPrice;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;

/**
 * Product price management api
 *
 * @author carlsamson
 */
public interface ProductPriceFacade {

	/**
	 * Creates a product price
	 */
	Long save(PersistableProductPrice price, StoreMerchantId store);

	/**
	 * Product price deletion
	 */
	void delete(Long priceId, String sku, StoreMerchantId store);

	/**
	 * List product prices by product and inventory (product and variants)
	 */
	List<ReadableProductPrice> list(String sku, Long inventoryId, StoreMerchantId store, LanguageCode language);

	/**
	 * List product prices by product
	 */
	List<ReadableProductPrice> list(String sku, StoreMerchantId store, LanguageCode language);

	/**
	 * Get ProductPrice
	 */
	ReadableProductPrice get(String sku, Long productPriceId, StoreMerchantId store, LanguageCode language);

}
