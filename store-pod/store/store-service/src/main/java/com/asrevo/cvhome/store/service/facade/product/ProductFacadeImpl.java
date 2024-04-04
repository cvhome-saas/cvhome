package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.ProductCriteria;
import com.asrevo.cvhome.store.core.entity.catalog.product.relationship.ProductRelationship;
import com.asrevo.cvhome.store.core.entity.catalog.product.relationship.ProductRelationshipType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductList;
import com.asrevo.cvhome.store.core.model.catalog.product.product.PersistableProduct;
import com.asrevo.cvhome.store.core.services.catalog.category.CategoryService;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductAttributeService;
import com.asrevo.cvhome.store.core.services.catalog.product.relationship.ProductRelationshipService;
import com.asrevo.cvhome.store.service.populator.catalog.ReadableProductPopulator;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service("productFacade")
//@Profile({"default", "cloud", "gcp", "aws", "mysql", "local"})
public class ProductFacadeImpl implements ProductFacade {

    private final CategoryService categoryService;

    private final ProductAttributeService productAttributeService;

    private final ProductService productService;

    private final PricingService pricingService;

    private final ProductRelationshipService productRelationshipService;


    private final ImageFilePath imageUtils;

    public ProductFacadeImpl(CategoryService categoryService, ProductAttributeService productAttributeService, ProductService productService, PricingService pricingService, ProductRelationshipService productRelationshipService, @Qualifier("img") ImageFilePath imageUtils) {
        this.categoryService = categoryService;
        this.productAttributeService = productAttributeService;
        this.productService = productService;
        this.pricingService = pricingService;
        this.productRelationshipService = productRelationshipService;
        this.imageUtils = imageUtils;
    }

    public void updateProduct(MerchantStore store, PersistableProduct product, Language language) {

        Assert.notNull(product, "Product must not be null");
        Assert.notNull(product.getId(), "Product id must not be null");

        // get original product
        productService.getById(product.getId());

    }

    @Override
    public ReadableProduct getProduct(MerchantStore store, String sku, Language language) throws Exception {

        Product product = productService.getBySku(sku, store, language);

        if (product == null) {
            return null;
        }

        ReadableProduct readableProduct = new ReadableProduct();

        ReadableProductPopulator populator = new ReadableProductPopulator();

        populator.setPricingService(pricingService);
        populator.setimageUtils(imageUtils);
        populator.populate(product, readableProduct, store, language);

        return readableProduct;
    }

    @Override
    public ReadableProductList getProductListsByCriterias(MerchantStore store, Language language,
                                                          ProductCriteria criterias) throws Exception {

        Assert.notNull(criterias, "ProductCriteria must be set for this product");

        /** This is for category **/
        if (CollectionUtils.isNotEmpty(criterias.getCategoryIds())) {

            if (criterias.getCategoryIds().size() == 1) {

                Category category = categoryService
                        .getById(criterias.getCategoryIds().get(0));

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

        List<Product> prds = products.stream().sorted(Comparator.comparing(Product::getSortOrder)).collect(Collectors.toList());
        products = prds;

        ReadableProductPopulator populator = new ReadableProductPopulator();
        populator.setPricingService(pricingService);
        populator.setimageUtils(imageUtils);

        ReadableProductList productList = new ReadableProductList();
        for (Product product : products) {

            // create new proxy product
            ReadableProduct readProduct = populator.populate(product, new ReadableProduct(), store, language);
            productList.getProducts().add(readProduct);

        }

        // productList.setTotalPages(products.getTotalCount());
        productList.setRecordsTotal(modelProductList.getTotalElements());
        productList.setNumber(productList.getProducts().size());

        productList.setTotalPages(modelProductList.getTotalPages());

        return productList;
    }

    @Override
    public ReadableProduct getProductByCode(MerchantStore store, String uniqueCode, Language language) {

        Product product = null;
        try {
            product = productService.getBySku(uniqueCode, store, language);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }

        ReadableProduct readableProduct = new ReadableProduct();

        ReadableProductPopulator populator = new ReadableProductPopulator();

        populator.setPricingService(pricingService);
        populator.setimageUtils(imageUtils);
        try {
            populator.populate(product, readableProduct, product.getMerchantStore(), language);
        } catch (ConversionException e) {
            throw new ConversionRuntimeException("Product with code [" + uniqueCode + "] cannot be converted", e);
        }

        return readableProduct;
    }

    @Override
    public List<ReadableProduct> relatedItems(MerchantStore store, Product product, Language language)
            throws Exception {
        ReadableProductPopulator populator = new ReadableProductPopulator();
        populator.setPricingService(pricingService);
        populator.setimageUtils(imageUtils);

        List<ProductRelationship> relatedItems = productRelationshipService.getByType(store, product,
                ProductRelationshipType.RELATED_ITEM);
        if (relatedItems != null && !relatedItems.isEmpty()) {
            List<ReadableProduct> items = new ArrayList<ReadableProduct>();
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


    @Override
    public ReadableProduct getProductBySeUrl(MerchantStore store, String friendlyUrl, Language language) throws Exception {

        Product product = productService.getBySeUrl(store, friendlyUrl, LocaleUtils.getLocale(language));

        if (product == null) {
            return null;
        }

        ReadableProduct readableProduct = new ReadableProduct();

        ReadableProductPopulator populator = new ReadableProductPopulator();

        populator.setPricingService(pricingService);
        populator.setimageUtils(imageUtils);
        populator.populate(product, readableProduct, store, language);

        return readableProduct;
    }

    /**
     * @Override public ReadableProductPrice getProductPrice(Long id, ProductPriceRequest priceRequest, MerchantStore store, Language language) {
     * Assert.notNull(id, "Product id cannot be null");
     * Assert.notNull(priceRequest, "Product price request cannot be null");
     * Assert.notNull(store, "MerchantStore cannot be null");
     * Assert.notNull(language, "Language cannot be null");
     * <p>
     * try {
     * Product model = productService.findOne(id, store);
     * <p>
     * //TODO check if null
     * List<Long> attrinutesIds = priceRequest.getOptions().stream().map(p -> p.getId()).collect(Collectors.toList());
     * <p>
     * List<ProductAttribute> attributes = productAttributeService.getByAttributeIds(store, model, attrinutesIds);
     * <p>
     * for(ProductAttribute attribute : attributes) {
     * if(attribute.getProduct().getId().longValue()!= id.longValue()) {
     * //throw unauthorized
     * throw new OperationNotAllowedException("Attribute with id [" + attribute.getId() + "] is not attached to product id [" + id + "]");
     * }
     * }
     * <p>
     * FinalPrice price;
     * <p>
     * price = pricingService.calculateProductPrice(model, attributes);
     * ReadableProductPrice readablePrice = new ReadableProductPrice();
     * ReadableFinalPricePopulator populator = new ReadableFinalPricePopulator();
     * populator.setPricingService(pricingService);
     * <p>
     * <p>
     * return populator.populate(price, readablePrice, store, language);
     * <p>
     * } catch (Exception e) {
     * throw new ServiceRuntimeException("An error occured while getting product price",e);
     * }
     * <p>
     * }
     **/

    @Override
    public Product getProduct(Long id, MerchantStore store) {
        return productService.findOne(id, store);
    }


}
