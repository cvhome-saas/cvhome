package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.product.PersistableProduct;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.PersistableProductMapper;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableProductPopulator;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import com.asrevo.cvhome.store.utils.PriceUtils;

/**
 * Version 1 Product management Version 2 Recommends using productVariant
 *
 * @author carlsamson
 */
@Service("productCommonFacade")
public class ProductCommonFacadeImpl implements ProductCommonFacade {

    private final ProductService productService;

    private final PricingService pricingService;

    private final PersistableProductMapper persistableProductMapper;

    private final ImageFilePath imageUtils;

    private final ExternalMerchantStoreService externalStoreMerchantIdService;

    public ProductCommonFacadeImpl(ProductService productService, PricingService pricingService,
                                   PersistableProductMapper persistableProductMapper, ImageFilePath imageUtils,
                                   ExternalMerchantStoreService externalStoreMerchantIdService) {
        this.productService = productService;
        this.pricingService = pricingService;
        this.persistableProductMapper = persistableProductMapper;
        this.imageUtils = imageUtils;
        this.externalStoreMerchantIdService = externalStoreMerchantIdService;
    }

    @Override
    public Long saveProduct(StoreMerchantId store, PersistableProduct product, LanguageCode language) {

        Product target;
        if (product.getId() != null && product.getId() > 0) {
            target = productService.getById(product.getId());
        } else {
            target = new Product();
        }

        try {

            target = persistableProductMapper.merge(product, target, store, language);
            target = productService.saveProduct(target);

            return target.getId();
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public ReadableProduct getProduct(StoreMerchantId store, Long id, LanguageCode language) {

        Product product = productService.findOne(id, store);
        if (product == null) {
            throw new ResourceNotFoundException("Product [" + id + "] not found");
        }

        if (!product.getStore().equals(store)) {
            throw new ResourceNotFoundException("Product [" + id + "] not found for store [" + store + "]");
        }

        ReadableProduct readableProduct = new ReadableProduct();
        ReadableProductPopulator populator = new ReadableProductPopulator(pricingService, imageUtils,
                externalStoreMerchantIdService);
        try {
            readableProduct = populator.populate(product, readableProduct, store, language);
        } catch (ConversionException e) {
            throw new ConversionRuntimeException("Error converting product [" + id + "]", e);
        }

        return readableProduct;
    }

    @Override
    public ReadableProduct addProductToCategory(Category category, Product product, LanguageCode language) {

        Assert.notNull(category, "Category cannot be null");
        Assert.notNull(product, "Product cannot be null");

        // not alloweed if category already attached
        List<Category> assigned = product.getCategories()
                .stream()
                .filter(cat -> cat.getId().longValue() == category.getId().longValue())
                .toList();

        if (!assigned.isEmpty()) {
            throw new OperationNotAllowedException("Category with id [" + category.getId()
                    + "] already attached to product [" + product.getId() + "]");
        }

        product.getCategories().add(category);
        ReadableProduct readableProduct = new ReadableProduct();

        try {

            productService.saveProduct(product);

            ReadableProductPopulator populator = new ReadableProductPopulator(pricingService, imageUtils,
                    externalStoreMerchantIdService);
            populator.populate(product, readableProduct, product.getStore(), language);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Exception when adding product [" + product.getId() + "] to category [" + category.getId() + "]",
                    e);
        }

        return readableProduct;
    }

    @Override
    public ReadableProduct removeProductFromCategory(Category category, Product product, LanguageCode language)
            throws Exception {

        Assert.notNull(category, "Category cannot be null");
        Assert.notNull(product, "Product cannot be null");

        product.getCategories().remove(category);
        productService.saveProduct(product);

        ReadableProduct readableProduct = new ReadableProduct();

        ReadableProductPopulator populator = new ReadableProductPopulator(pricingService, imageUtils,
                externalStoreMerchantIdService);
        populator.populate(product, readableProduct, product.getStore(), language);

        return readableProduct;
    }

    @Override
    public void update(Long productId, LightPersistableProduct product, StoreMerchantId merchant,
                       LanguageCode language) {
        // Get product
        Product modified = productService.findOne(productId, merchant);

        // Update product with minimal set
        modified.setAvailable(product.isAvailable());

        for (ProductAvailability availability : modified.getAvailabilities()) {
            availability.setProductQuantity(product.getQuantity());
            if (!StringUtils.isBlank(product.getPrice())) {
                // set default price
                for (ProductPrice price : availability.getPrices()) {
                    if (price.isDefaultPrice()) {
                        try {
                            price.setProductPriceAmount(PriceUtils.getAmount(product.getPrice()));
                        } catch (ServiceException e) {
                            throw new ServiceRuntimeException("Invalid product price format");
                        }
                    }
                }
            }
        }

        productService.save(modified);
    }

    @Override
    public boolean exists(String sku, StoreMerchantId store) {

        return productService.exists(sku, store);
    }

    @Override
    public void deleteProduct(Long id, StoreMerchantId store) {

        Assert.notNull(id, "Product id cannot be null");
        Assert.notNull(store, "store cannot be null");

        Product p = productService.getById(id);

        if (p == null) {
            throw new ResourceNotFoundException("Product with id [" + id + " not found");
        }

        if (!Objects.equals(p.getStore(), store)) {
            throw new ResourceNotFoundException("Product with id [" + id + " not found for store [" + store + "]");
        }

        try {
            productService.delete(p);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while deleting ptoduct with id [" + id + "]", e);
        }
    }

    @Override
    public void update(String sku, LightPersistableProduct product, StoreMerchantId store, LanguageCode language) {
        // Get product
        Product modified;
        try {
            modified = productService.getBySku(sku, store, language);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }

        ProductVariant instance = modified.getVariants()
                .stream()
                .filter(inst -> sku.equals(inst.getSku()))
                .findAny()
                .orElse(null);

        if (instance != null) {
            instance.setAvailable(product.isAvailable());

            for (ProductAvailability availability : instance.getAvailabilities()) {
                this.setAvailability(availability, product);
            }
        } else {
            // Update product with minimal set
            modified.setAvailable(product.isAvailable());

            for (ProductAvailability availability : modified.getAvailabilities()) {
                this.setAvailability(availability, product);
            }
        }

        try {
            productService.saveProduct(modified);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannot update product ", e);
        }
    }

    /**
     * edit availability
     */
    private void setAvailability(ProductAvailability availability, LightPersistableProduct product) {
        availability.setProductQuantity(product.getQuantity());
        if (!StringUtils.isBlank(product.getPrice())) {
            // set default price
            for (ProductPrice price : availability.getPrices()) {
                if (price.isDefaultPrice()) {
                    try {
                        price.setProductPriceAmount(PriceUtils.getAmount(product.getPrice()));
                    } catch (ServiceException e) {
                        throw new ServiceRuntimeException("Invalid product price format");
                    }
                }
            }
        }
    }

}
