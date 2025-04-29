package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductVariantMapper;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductAttributeService;
import com.asrevo.cvhome.catalog.services.product.availability.ProductAvailabilityService;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("productFacadeV2")
// @Profile({"default", "cloud", "gcp", "aws", "mysql", "local"})
public class ProductFacadeV2Impl implements ProductFacade {

    private final ProductService productService;

    private final CategoryService categoryService;

    private final ReadableProductMapper readableProductMapper;

    private final ProductVariantService productVariantService;

    private final ReadableProductVariantMapper readableProductVariantMapper;

    public ProductFacadeV2Impl(
            ProductService productService,
            CategoryService categoryService,
            ReadableProductMapper readableProductMapper,
            ProductVariantService productVariantService,
            ReadableProductVariantMapper readableProductVariantMapper,
            ProductAvailabilityService productAvailabilityService,
            ProductAttributeService productAttributeService,
            PricingService pricingService,
            ImageFilePath imageUtils) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.readableProductMapper = readableProductMapper;
        this.productVariantService = productVariantService;
        this.readableProductVariantMapper = readableProductVariantMapper;
    }

    @Override
    public Product getProduct(Long id, StoreMerchantId store) {
        // same as v1
        return productService.findOne(id, store);
    }

    private ReadableProductVariant productVariant(
            ProductVariant instance, StoreMerchantId store, LanguageCode language) {

        return readableProductVariantMapper.convert(instance, store, language);
    }

    @Override
    public ReadableProduct getProductBySeUrl(
            StoreMerchantId store, String friendlyUrl, LanguageCode language) {

        Product product =
                productService.getBySeUrl(store, friendlyUrl, LocaleUtils.getLocale(language));

        if (product == null) {
            throw new ResourceNotFoundException(
                    "Product [" + friendlyUrl + "] not found for merchant [" + store + "]");
        }

        ReadableProduct readableProduct = readableProductMapper.convert(product, store, language);

        // get all instances for this product group by option
        // limit to 15 searches
        List<ProductVariant> instances =
                productVariantService.getByProductId(store, product, language);

        // the above get all possible images
        List<ReadableProductVariant> readableInstances =
                instances.stream()
                        .map(p -> this.productVariant(p, store, language))
                        .collect(Collectors.toList());
        readableProduct.setVariants(readableInstances);

        return readableProduct;
    }

    /**
     * Filters on otion, optionValues and other criterias
     */
    @Override
    public ReadableProductList getProductListsByCriterias(
            StoreMerchantId store, LanguageCode language, ProductCriteria criterias)
            throws Exception {
        Assert.notNull(criterias, "ProductCriteria must be set for this product");

        if (CollectionUtils.isNotEmpty(criterias.getCategoryIds())) {

            if (criterias.getCategoryIds().size() == 1) {

                Category category = categoryService.getById(criterias.getCategoryIds().getFirst());

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
                    criterias.setCategoryIds(ids);
                }
            }
        }

        Page<Product> modelProductList =
                productService.listByStore(
                        store,
                        language,
                        criterias,
                        criterias.getStartPage(),
                        criterias.getMaxCount());

        List<Product> products = modelProductList.getContent();
        ReadableProductList productList = new ReadableProductList();

        List<ReadableProduct> readableProducts =
                products.stream()
                        .map(p -> readableProductMapper.convert(p, store, language))
                        .sorted(Comparator.comparing(ReadableProduct::getSortOrder))
                        .collect(Collectors.toList());

        productList.setRecordsTotal(modelProductList.getTotalElements());
        productList.setNumber(modelProductList.getNumberOfElements());
        productList.setProducts(readableProducts);
        productList.setTotalPages(modelProductList.getTotalPages());

        return productList;
    }
}
