package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.errors.EntitlementExceededException;
import com.asrevo.cvhome.billing.guard.StoreEntitlements;
import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.errors.CategoryAlreadyAttachedException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotPersistedException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.product.PersistableProduct;
import com.asrevo.cvhome.catalog.repositories.product.ProductRepository;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.PersistableProductMapper;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableProductPopulator;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.utils.ImageFilePath;

/**
 * Version 1 Product management Version 2 Recommends using productVariant
 *
 * @author carlsamson
 */
@Service("productCommonFacade")
public class ProductCommonFacadeImpl implements ProductCommonFacade {

    private final ProductService productService;

    private final PersistableProductMapper persistableProductMapper;

    private final ImageFilePath imageUtils;

    private final ExternalMerchantStoreService externalStoreMerchantIdService;

    private final StoreEntitlements storeEntitlements;

    private final ProductRepository productRepository;

    public ProductCommonFacadeImpl(ProductService productService,
                                   PersistableProductMapper persistableProductMapper, ImageFilePath imageUtils,
                                   ExternalMerchantStoreService externalStoreMerchantIdService,
                                   StoreEntitlements storeEntitlements, ProductRepository productRepository) {
        this.productService = productService;
        this.persistableProductMapper = persistableProductMapper;
        this.imageUtils = imageUtils;
        this.externalStoreMerchantIdService = externalStoreMerchantIdService;
        this.storeEntitlements = storeEntitlements;
        this.productRepository = productRepository;
    }

    @Override
    public Long saveProduct(StoreMerchantId store, PersistableProduct product, LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException,
            ProductNotPersistedException, EntitlementExceededException {

        Product target;
        if (product.getId() != null && product.getId() > 0) {
            target = productService.getById(product.getId());
        } else {
            // Only a new product can take the store past its ceiling; editing one cannot. The count is behind a
            // supplier so a plan with no product limit never runs it.
            storeEntitlements.require(store, EntitlementKey.MAX_PRODUCTS, () -> productRepository.countByStore(store));
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
        ReadableProductPopulator populator = new ReadableProductPopulator(imageUtils,
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

        ReadableProductPopulator populator = new ReadableProductPopulator(imageUtils,
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

        ReadableProductPopulator populator = new ReadableProductPopulator(imageUtils,
                externalStoreMerchantIdService);
        populator.populate(product, readableProduct, product.getStore(), language);

        return readableProduct;
    }

    @Override
    public void update(Long productId, LightPersistableProduct product, StoreMerchantId merchant,
                       LanguageCode language) {
        // Get product
        Product modified = productService.findOne(productId, merchant);

        // Update product with minimal set; price and quantity belong to the inventory service since the split
        modified.setAvailable(product.isAvailable());

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

        // Update product with minimal set; price and quantity belong to the inventory service since the split
        modified.setAvailable(product.isAvailable());

        productService.saveProduct(modified);
    }

}
