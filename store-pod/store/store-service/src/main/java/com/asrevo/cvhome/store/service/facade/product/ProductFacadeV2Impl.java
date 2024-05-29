package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.ProductCriteria;
import com.asrevo.cvhome.store.core.entity.catalog.product.relationship.ProductRelationship;
import com.asrevo.cvhome.store.core.entity.catalog.product.relationship.ProductRelationshipType;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductList;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.store.core.services.catalog.category.CategoryService;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductAttributeService;
import com.asrevo.cvhome.store.core.services.catalog.product.availability.ProductAvailabilityService;
import com.asrevo.cvhome.store.core.services.catalog.product.relationship.ProductRelationshipService;
import com.asrevo.cvhome.store.core.services.catalog.product.variant.ProductVariantService;
import com.asrevo.cvhome.store.service.mapper.catalog.product.ReadableProductMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.product.ReadableProductVariantMapper;
import com.asrevo.cvhome.store.service.populator.catalog.ReadableProductPopulator;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service("productFacadeV2")
//@Profile({"default", "cloud", "gcp", "aws", "mysql", "local"})
public class ProductFacadeV2Impl implements ProductFacade {


    private final ProductService productService;

    private final CategoryService categoryService;

    private final ProductRelationshipService productRelationshipService;

    private final ReadableProductMapper readableProductMapper;

    private final ProductVariantService productVariantService;

    private final ReadableProductVariantMapper readableProductVariantMapper;

    private final PricingService pricingService;

    private final ImageFilePath imageUtils;

    public ProductFacadeV2Impl(ProductService productService, CategoryService categoryService, ProductRelationshipService productRelationshipService, ReadableProductMapper readableProductMapper, ProductVariantService productVariantService, ReadableProductVariantMapper readableProductVariantMapper, ProductAvailabilityService productAvailabilityService, ProductAttributeService productAttributeService, PricingService pricingService, ImageFilePath imageUtils) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.productRelationshipService = productRelationshipService;
        this.readableProductMapper = readableProductMapper;
        this.productVariantService = productVariantService;
        this.readableProductVariantMapper = readableProductVariantMapper;
        this.pricingService = pricingService;
        this.imageUtils = imageUtils;
    }


    @Override
    public Product getProduct(Long id, MerchantStore store) {
        //same as v1
        return productService.findOne(id, store);
    }

    @Override
    public ReadableProduct getProductByCode(MerchantStore store, String sku, Language language) {


        Product product = null;
        try {
            product = productService.getBySku(sku, store, language);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }

        if (product == null) {
            throw new ResourceNotFoundException("Product [" + sku + "] not found for merchant [" + store.getCode() + "]");
        }

        if (!product.getMerchantStore().getId().equals(store.getId())) {
            throw new ResourceNotFoundException("Product [" + sku + "] not found for merchant [" + store.getCode() + "]");
        }


        ReadableProduct readableProduct = readableProductMapper.convert(product, store, language);

        return readableProduct;

    }

    private ReadableProductVariant productVariant(ProductVariant instance, MerchantStore store, Language language) {

        return readableProductVariantMapper.convert(instance, store, language);

    }

    @Override
    public ReadableProduct getProduct(MerchantStore store, String sku, Language language) throws Exception {
        return this.getProductByCode(store, sku, language);
    }

    @Override
    public ReadableProduct getProductBySeUrl(MerchantStore store, String friendlyUrl, Language language)
            throws Exception {

        Product product = productService.getBySeUrl(store, friendlyUrl, LocaleUtils.getLocale(language));

        if (product == null) {
            throw new ResourceNotFoundException("Product [" + friendlyUrl + "] not found for merchant [" + store.getCode() + "]");
        }

        ReadableProduct readableProduct = readableProductMapper.convert(product, store, language);

        //get all instances for this product group by option
        //limit to 15 searches
        List<ProductVariant> instances = productVariantService.getByProductId(store, product, language);

        //the above get all possible images
        List<ReadableProductVariant> readableInstances = instances.stream().map(p -> this.productVariant(p, store, language)).collect(Collectors.toList());
        readableProduct.setVariants(readableInstances);

        return readableProduct;

    }

    /**
     * Filters on otion, optionValues and other criterias
     */

    @Override
    public ReadableProductList getProductListsByCriterias(MerchantStore store, Language language,
                                                          ProductCriteria criterias) throws Exception {
        Assert.notNull(criterias, "ProductCriteria must be set for this product");

        if (CollectionUtils.isNotEmpty(criterias.getCategoryIds())) {

            if (criterias.getCategoryIds().size() == 1) {

                Category category = categoryService
                        .getById(criterias.getCategoryIds().getFirst());

                if (category != null) {
                    String lineage = category.getLineage();

                    List<Category> categories = categoryService
                            .getListByLineage(store, lineage);

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


        Page<Product> modelProductList = productService.listByStore(store, language, criterias, criterias.getStartPage(), criterias.getMaxCount());

        List<Product> products = modelProductList.getContent();
        ReadableProductList productList = new ReadableProductList();


        List<ReadableProduct> readableProducts = products.stream().map(p -> readableProductMapper.convert(p, store, language))
                .sorted(Comparator.comparing(ReadableProduct::getSortOrder)).collect(Collectors.toList());


        productList.setRecordsTotal(modelProductList.getTotalElements());
        productList.setNumber(modelProductList.getNumberOfElements());
        productList.setProducts(readableProducts);
        productList.setTotalPages(modelProductList.getTotalPages());

        return productList;
    }

    @Override
    public List<ReadableProduct> relatedItems(MerchantStore store, Product product, Language language)
            throws Exception {
        //same as v1
        ReadableProductPopulator populator = new ReadableProductPopulator();
        populator.setPricingService(pricingService);
        populator.setImageUtils(imageUtils);

        List<ProductRelationship> relatedItems = productRelationshipService.getByType(store, product,
                ProductRelationshipType.RELATED_ITEM);
        if (relatedItems != null && !relatedItems.isEmpty()) {
            List<ReadableProduct> items = new ArrayList<>();
            for (ProductRelationship relationship : relatedItems) {
                Product relatedProduct = relationship.getRelatedProduct();
                ReadableProduct proxyProduct = populator.populate(relatedProduct, new ReadableProduct(), store,
                        language);
                items.add(proxyProduct);
            }
            return items;
        }
        return null;
    }


}
