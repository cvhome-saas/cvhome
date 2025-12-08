package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableProductPopulator;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import org.springframework.stereotype.Service;

@Service("productFacade")
// @Profile({"default", "cloud", "gcp", "aws", "mysql", "local"})
public class ProductFacadeImpl implements ProductFacade {

	private final ProductService productService;

	private final PricingService pricingService;

	private final ImageFilePath imageUtils;

	private final ExternalMerchantStoreService externalMerchantStoreService;

	public ProductFacadeImpl(ProductService productService, PricingService pricingService, ImageFilePath imageUtils,
			ExternalMerchantStoreService externalStoreMerchantIdService) {
		this.productService = productService;
		this.pricingService = pricingService;
		this.imageUtils = imageUtils;
		this.externalMerchantStoreService = externalStoreMerchantIdService;
	}

	@Override
	public ReadableProductList getProductListsByCriteria(StoreMerchantId store, ProductCriteria criteria) {
		return null;
	}

	@Override
	public ReadableProductList getBaseProductListsByCriteria(StoreMerchantId merchantStore,
			ProductCriteria searchCriteria) {
		return null;
	}

	@Override
	public ReadableProduct getProductBySeUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language)
			throws Exception {

		Product product = productService.getBySeUrl(store, friendlyUrl, LocaleUtils.getLocale(language));

		if (product == null) {
			return null;
		}

		ReadableProduct readableProduct = new ReadableProduct();

		ReadableProductPopulator populator = new ReadableProductPopulator(pricingService, imageUtils,
				externalMerchantStoreService);
		populator.populate(product, readableProduct, store, language);

		return readableProduct;
	}

	@Override
	public Product getProduct(Long id, StoreMerchantId store) {
		return productService.findOne(id, store);
	}

}
