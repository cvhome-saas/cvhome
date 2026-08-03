package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.errors.CategoryAlreadyAttachedException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotPersistedException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductVariantSkuConflictException;
import com.asrevo.cvhome.catalog.errors.ProductVariationReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.product.PersistableProduct;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.PersistableProductMapper;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableProductPopulator;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.UncheckedBaseException;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.errors.NonPositivePriceException;
import com.asrevo.cvhome.store.errors.PriceNotParseableException;
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
    public Long saveProduct(StoreMerchantId store, PersistableProduct product, LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException,
            ProductVariationReferenceUnresolvableException, ProductReferenceUnresolvableException,
            ProductVariantSkuConflictException, ProductPriceNotConvertibleException,
            InventoryNotConvertibleException, ProductNotPersistedException {

        Product target;
        if (product.getId() != null && product.getId() > 0) {
            target = productService.getById(product.getId());
        } else {
            target = new Product();
        }

        target = persistableProductMapper.merge(product, target, store, language);
        target = productService.saveProduct(target);

        return target.getId();
    }

    @Override
    public ReadableProduct getProduct(StoreMerchantId store, Long id, LanguageCode language)
            throws ProductNotFoundException, ProductNotConvertibleException {

        Product product = productService.findOne(id, store);
        if (product == null || !product.getStore().equals(store)) {
            throw ProductNotFoundException.of(id, store);
        }

        ReadableProduct readableProduct = new ReadableProduct();
        ReadableProductPopulator populator = new ReadableProductPopulator(pricingService, imageUtils,
                externalStoreMerchantIdService);
        return populator.populate(product, readableProduct, store, language);
    }

    @Override
    public ReadableProduct addProductToCategory(Category category, Product product, LanguageCode language)
            throws CategoryAlreadyAttachedException, ProductNotConvertibleException, ProductNotPersistedException {
        List<Category> assigned = product.getCategories()
                .stream()
                .filter(cat -> cat.getId().longValue() == category.getId().longValue())
                .toList();

        if (!assigned.isEmpty()) {
            throw CategoryAlreadyAttachedException.of(category.getId(), product.getId());
        }

        product.getCategories().add(category);
        ReadableProduct readableProduct = new ReadableProduct();

        productService.saveProduct(product);

        ReadableProductPopulator populator = new ReadableProductPopulator(pricingService, imageUtils,
                externalStoreMerchantIdService);
        populator.populate(product, readableProduct, product.getStore(), language);

        return readableProduct;
    }

    @Override
    public ReadableProduct removeProductFromCategory(Category category, Product product, LanguageCode language)
            throws Exception {
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
                        } catch (PriceNotParseableException | NonPositivePriceException e) {
                            // Carried unchecked because this method cannot declare it yet; the advice unwraps the
                            // carrier, so the precise price code survives instead of collapsing into a generic one.
                            throw new UncheckedBaseException(e);
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
    public void deleteProduct(Long id, StoreMerchantId store) throws ProductNotFoundException {
        Product p = productService.getById(id);

        if (p == null || !Objects.equals(p.getStore(), store)) {
            throw ProductNotFoundException.of(id, store);
        }

        productService.delete(p);
    }

    @Override
    public void update(String sku, LightPersistableProduct product, StoreMerchantId store, LanguageCode language)
            throws ProductNotFoundException, ProductNotPersistedException {
        // Get product
        Product modified = productService.getBySku(sku, store, language);

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

        productService.saveProduct(modified);
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
                    } catch (PriceNotParseableException | NonPositivePriceException e) {
                        throw new UncheckedBaseException(e);
                    }
                }
            }
        }
    }

}
