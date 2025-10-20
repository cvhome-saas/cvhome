package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableBaseProductMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductVariantMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableTinyProductMapper;
import com.asrevo.cvhome.catalog.services.category.CategoryServiceImpl;
import com.asrevo.cvhome.catalog.services.pricing.PricingServiceImpl;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("productFacadeV2")
public class ProductFacadeV2Impl implements ProductFacade {

	private final ProductService productService;

	private final ReadableProductMapper readableProductMapper;

	private final ProductVariantService productVariantService;

	private final ReadableProductVariantMapper readableProductVariantMapper;

	private final CategoryServiceImpl categoryService;

	private final PricingServiceImpl pricingService;

	public ProductFacadeV2Impl(ProductService productService, ReadableProductMapper readableProductMapper,
			ProductVariantService productVariantService, ReadableProductVariantMapper readableProductVariantMapper,
			CategoryServiceImpl categoryService, PricingServiceImpl pricingService) {
		this.productService = productService;
		this.readableProductMapper = readableProductMapper;
		this.productVariantService = productVariantService;
		this.readableProductVariantMapper = readableProductVariantMapper;
		this.categoryService = categoryService;
		this.pricingService = pricingService;
	}

	@Override
	public Product getProduct(Long id, StoreMerchantId store) {
		// same as v1
		return productService.findOne(id, store);
	}

	private ReadableProductVariant productVariant(ProductVariant instance, StoreMerchantId store,
			LanguageCode language) {

		return readableProductVariantMapper.convert(instance, store, language);
	}

	@Override
	public ReadableProduct getProductBySeUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language) {

		Product product = productService.getBySeUrl(store, friendlyUrl, LocaleUtils.getLocale(language));

		if (product == null) {
			throw new ResourceNotFoundException("Product [" + friendlyUrl + "] not found for merchant [" + store + "]");
		}

		ReadableProduct readableProduct = readableProductMapper.convert(product, store, language);

		// get all instances for this product group by option
		// limit to 15 searches
		List<ProductVariant> instances = productVariantService.getByProductId(store, product, language);

		// the above get all possible images
		List<ReadableProductVariant> readableInstances = instances.stream()
			.map(p -> this.productVariant(p, store, language))
			.collect(Collectors.toList());
		readableProduct.setVariants(readableInstances);

		return readableProduct;
	}

	/**
	 * Filters on otion, optionValues and other criterias
	 */
	@SneakyThrows
	@Override
	public ReadableProductList getProductListsByCriteria(StoreMerchantId merchantStore,
			ProductCriteria searchCriteria) {
		Assert.notNull(searchCriteria, "ProductCriteria must be set for this product");
		return listProducts(readableProductMapper, merchantStore, searchCriteria);
	}

	@Override
	public ReadableProductList getTinyProductListsByCriteria(StoreMerchantId merchantStore,
			ProductCriteria searchCriteria) {
		return listProducts(new ReadableTinyProductMapper(), merchantStore, searchCriteria);
	}

	@Override
	public ReadableProductList getBaseProductListsByCriteria(StoreMerchantId merchantStore,
			ProductCriteria searchCriteria) {
		return listProducts(new ReadableBaseProductMapper(pricingService), merchantStore, searchCriteria);
	}

	@SneakyThrows
	ReadableProductList listProducts(Mapper<Product, ReadableProduct> mapper, StoreMerchantId store,
			ProductCriteria criteria) {
		if (CollectionUtils.isNotEmpty(criteria.getCategoryIds())) {

			if (criteria.getCategoryIds().size() == 1) {

				Category category = categoryService.getById(criteria.getCategoryIds().getFirst());

				if (category != null) {
					String lineage = category.getLineage();

					List<Category> categories = categoryService.getListByLineage(store, lineage);

					List<Long> ids = new ArrayList<>();
					if (categories != null && !categories.isEmpty()) {
						for (Category c : categories) {
							ids.add(c.getId());
						}
					}
					ids.add(category.getId());
					criteria.setCategoryIds(ids);
				}
			}
		}

		Page<Product> all = productService.findAll(criteria, store);

		ReadableProductList readableProductList = new ReadableProductList();
		List<ReadableProduct> readableProducts = all.getContent()
			.stream()
			.map(p -> mapper.convert(p, store, criteria.getLanguage()))
			.sorted(Comparator.comparing(ReadableProduct::getSortOrder))
			.collect(Collectors.toList());

		readableProductList.setTotalElements(all.getTotalElements());
		readableProductList.setSize(all.getNumberOfElements());
		readableProductList.setContent(readableProducts);
		readableProductList.setTotalPages(all.getTotalPages());

		return readableProductList;
	}

}
