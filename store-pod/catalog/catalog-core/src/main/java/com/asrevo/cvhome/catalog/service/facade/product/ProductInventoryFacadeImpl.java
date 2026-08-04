package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.InventoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductVariantNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.inventory.PersistableInventory;
import com.asrevo.cvhome.catalog.model.product.inventory.ReadableInventory;
import com.asrevo.cvhome.catalog.service.mapper.inventory.PersistableInventoryMapper;
import com.asrevo.cvhome.catalog.service.mapper.inventory.ReadableInventoryMapper;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.availability.ProductAvailabilityService;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import static com.asrevo.cvhome.store.utils.ReadableEntityUtil.createReadableList;

@Service("productInventoryFacade")
public class ProductInventoryFacadeImpl implements ProductInventoryFacade {

    private final ProductAvailabilityService productAvailabilityService;

    private final ProductService productService;

    private final ProductVariantService productVariantService;

    private final ReadableInventoryMapper readableInventoryMapper;

    private final PersistableInventoryMapper productInventoryMapper;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public ProductInventoryFacadeImpl(ProductAvailabilityService productAvailabilityService,
                                      ProductService productService, ProductVariantService productVariantService,
                                      ReadableInventoryMapper readableInventoryMapper, PersistableInventoryMapper productInventoryMapper,
                                      ExternalMerchantStoreService externalMerchantStoreService) {
        this.productAvailabilityService = productAvailabilityService;
        this.productService = productService;
        this.productVariantService = productVariantService;
        this.readableInventoryMapper = readableInventoryMapper;
        this.productInventoryMapper = productInventoryMapper;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public void delete(Long productId, Long inventoryId, StoreMerchantId store)
            throws InventoryNotFoundException {
        ProductAvailability availability = productAvailabilityService.getById(inventoryId, store)
                .filter(it -> it.getProduct().getId().equals(productId))
                .orElseThrow(() -> InventoryNotFoundException.of(inventoryId, store));
        productAvailabilityService.delete(availability);
    }

    private Product getProductById(Long productId, StoreMerchantId store) throws ProductNotFoundException {
        return productService.retrieveById(productId, store)
                .orElseThrow(() -> ProductNotFoundException.of(productId, store));
    }

    private ProductVariant getProductByInstance(Long instanceId, StoreMerchantId store)
            throws ProductVariantNotFoundException {
        return productVariantService.getById(instanceId, store)
                .orElseThrow(() -> ProductVariantNotFoundException.of(instanceId, store));
    }

    @Override
    public ReadableInventory add(PersistableInventory inventory, StoreMerchantId store, LanguageCode language)
            throws InventoryNotConvertibleException, InventoryReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductVariantReferenceUnresolvableException {
        ProductAvailability availability = getProductAvailabilityToSave(inventory, store);

        // add inventory to the product

        productAvailabilityService.saveOrUpdate(availability);
        return readableInventoryMapper.convert(availability, store, language);
    }

    private ProductAvailability getProductAvailabilityToSave(PersistableInventory inventory, StoreMerchantId store)
            throws InventoryNotConvertibleException, InventoryReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductVariantReferenceUnresolvableException {
        LanguageCode defaultLanguage = externalMerchantStoreService.getStore(store).getDefaultLanguage();
        return productInventoryMapper.convert(inventory, store, defaultLanguage);
    }

    @Override
    public ReadableInventory get(Long inventoryId, StoreMerchantId store, LanguageCode language)
            throws InventoryNotFoundException, InventoryNotConvertibleException {

        ProductAvailability availability = productAvailabilityService.getById(inventoryId, store)
                .orElseThrow(() -> InventoryNotFoundException.of(inventoryId, store));
        return readableInventoryMapper.convert(availability, store, language);
    }

    @Override
    public void update(PersistableInventory inventory, StoreMerchantId store, LanguageCode language)
            throws InventoryNotFoundException, InventoryNotConvertibleException,
            InventoryReferenceUnresolvableException, ProductNotFoundException, ProductReferenceUnresolvableException,
            ProductVariantNotFoundException, ProductVariantReferenceUnresolvableException {
        Set<ProductAvailability> originAvailability = null;
        Product product = null;

        if (inventory.getProductId() != null && inventory.getProductId() > 0) {
            product = this.getProductById(inventory.getProductId(), store);
            originAvailability = product.getAvailabilities();
        } else {
            if (inventory.getVariant() != null && inventory.getId() > 0) {
                ProductVariant instance = this.getProductByInstance(inventory.getVariant(), store);
                originAvailability = instance.getAvailabilities();
                product = instance.getProduct();
            }
        }

        ProductAvailability avail = Optional.ofNullable(originAvailability)
                .flatMap(it -> it.stream().filter(a -> a.getId().equals(inventory.getId())).findAny())
                .orElse(null);
        if (avail == null) {
            throw InventoryNotFoundException.of(inventory.getId(), store);
        }

        if (product != null) {

            inventory.setProductId(product.getId());
        }

        avail = productInventoryMapper.merge(inventory, avail, store, language);
        avail.setProduct(product);
        avail.setStoreMerchantId(store);
        productAvailabilityService.saveOrUpdate(avail);
    }

    @Override
    public ReadableEntityList<ReadableInventory> get(String sku, StoreMerchantId store, LanguageCode language,
                                                     Pageable pageable)
            throws ProductNotFoundException, InventoryNotConvertibleException {
        Page<ProductAvailability> availabilities = productAvailabilityService.getBySku(sku, pageable);

        if (availabilities.isEmpty()) {
            // get parent product; an unknown sku is now a 404 naming it, rather than the 500 the ServiceException
            // wrapper produced
            Product singleProduct = productService.getBySku(sku, store);
            if (singleProduct != null) {
                availabilities = new PageImpl<>(new ArrayList<>(singleProduct.getAvailabilities()));
            }
        }

        return createReadableList(availabilities, toReadable(availabilities, store, language));
    }

    @Override
    public ReadableEntityList<ReadableInventory> get(Long productId, StoreMerchantId store, LanguageCode language,
                                                     Pageable pageable) throws InventoryNotConvertibleException {
        Page<ProductAvailability> availabilities = productAvailabilityService.listByProduct(productId, store, pageable);

        return createReadableList(availabilities, toReadable(availabilities, store, language));
    }

    /**
     * Plain loops rather than {@code stream().map(...)}: the inventory mapper declares a checked failure now, and a
     * lambda cannot carry it out to the caller's signature.
     */
    private List<ReadableInventory> toReadable(Page<ProductAvailability> availabilities, StoreMerchantId store,
                                               LanguageCode language) throws InventoryNotConvertibleException {
        List<ReadableInventory> readable = new ArrayList<>();
        for (ProductAvailability availability : availabilities.getContent()) {
            readable.add(readableInventoryMapper.convert(availability, store, language));
        }
        return readable;
    }

}
