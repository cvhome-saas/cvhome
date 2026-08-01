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
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import static com.asrevo.cvhome.store.utils.ReadableEntityUtil.createReadableList;

@Service("productInventoryFacade")
public class ProductInventoryFacadeImpl implements ProductInventoryFacade {

    private static final String PRODUCT_INVENTORY_NOT_FOUND_TEMPLATE =
            "Product with id [%s] and inventory id [%s] not found for store id [%s]";

    private static final String PRODUCT_WITH_ID_NOT_FOUND_TEMPLATE = "Product with id [%s] not found";

    private static final String PRODUCT_WITH_INSTANCE_NOT_FOUND_TEMPLATE = "Product with instance [%s] not found";

    private static final String INVENTORY_WITH_ID_NOT_FOUND_TEMPLATE = "Inventory with id [%s] not found";

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
    public void delete(Long productId, Long inventoryId, StoreMerchantId store) {
        Optional<ProductAvailability> availability = productAvailabilityService.getById(inventoryId, store);
        try {
            if (availability.isPresent()) {
                if (availability.get().getProduct().getId().equals(productId)) {
                    productAvailabilityService.delete(availability.get());
                } else {
                    throw new ResourceNotFoundException(
                            PRODUCT_INVENTORY_NOT_FOUND_TEMPLATE.formatted(productId, inventoryId, store));
                }
            } else {
                throw new ResourceNotFoundException(
                        PRODUCT_INVENTORY_NOT_FOUND_TEMPLATE.formatted(productId, inventoryId, store));
            }
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while deleting inventory", e);
        }
    }

    private Product getProductById(Long productId, StoreMerchantId store) {
        return productService.retrieveById(productId, store)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_WITH_ID_NOT_FOUND_TEMPLATE.formatted(productId)));
    }

    private ProductVariant getProductByInstance(Long instanceId, StoreMerchantId store) {
        return productVariantService.getById(instanceId, store)
                .orElseThrow(
                        () -> new ResourceNotFoundException(PRODUCT_WITH_INSTANCE_NOT_FOUND_TEMPLATE.formatted(instanceId)));
    }

    @Override
    public ReadableInventory add(PersistableInventory inventory, StoreMerchantId store, LanguageCode language) {
        ProductAvailability availability = getProductAvailabilityToSave(inventory, store);

        // add inventory to the product

        saveOrUpdate(availability);
        return readableInventoryMapper.convert(availability, store, language);
    }

    private void saveOrUpdate(ProductAvailability availability) {
        try {
            productAvailabilityService.saveOrUpdate(availability);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Cannot create Inventory", e);
        }
    }

    private ProductAvailability getProductAvailabilityToSave(PersistableInventory inventory, StoreMerchantId store) {
        LanguageCode defaultLanguage = externalMerchantStoreService.getStore(store).getDefaultLanguage();
        return productInventoryMapper.convert(inventory, store, defaultLanguage);
    }

    @Override
    public ReadableInventory get(Long inventoryId, StoreMerchantId store, LanguageCode language) {

        ProductAvailability availability = productAvailabilityService.getById(inventoryId, store)
                .orElseThrow(
                        () -> new ResourceNotFoundException(INVENTORY_WITH_ID_NOT_FOUND_TEMPLATE.formatted(inventoryId)));
        return readableInventoryMapper.convert(availability, store, language);
    }

    @Override
    public void update(PersistableInventory inventory, StoreMerchantId store, LanguageCode language) {
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
            throw new ResourceNotFoundException(INVENTORY_WITH_ID_NOT_FOUND_TEMPLATE.formatted(inventory.getId()));
        }

        if (product != null) {

            inventory.setProductId(product.getId());
        }

        avail = productInventoryMapper.merge(inventory, avail, store, language);
        avail.setProduct(product);
        avail.setStoreMerchantId(store);
        saveOrUpdate(avail);
    }

    @Override
    public ReadableEntityList<ReadableInventory> get(String sku, StoreMerchantId store, LanguageCode language,
                                                     Pageable pageable) {
        Page<ProductAvailability> availabilities = productAvailabilityService.getBySku(sku, pageable);

        if (availabilities.isEmpty()) {
            // get parent product
            try {
                Product singleProduct = productService.getBySku(sku, store);
                if (singleProduct != null) {
                    availabilities = new PageImpl<>(new ArrayList<>(singleProduct.getAvailabilities()));
                }
            } catch (ServiceException e) {
                throw new ServiceRuntimeException("An error occured while getting product with sku " + sku, e);
            }
        }

        List<ReadableInventory> returnList = availabilities.getContent()
                .stream()
                .map(i -> this.readableInventoryMapper.convert(i, store, language))
                .toList();

        return createReadableList(availabilities, returnList);
    }

    @Override
    public ReadableEntityList<ReadableInventory> get(Long productId, StoreMerchantId store, LanguageCode language,
                                                     Pageable pageable) {
        Page<ProductAvailability> availabilities = productAvailabilityService.listByProduct(productId, store, pageable);

        List<ReadableInventory> returnList = availabilities.getContent()
                .stream()
                .map(i -> this.readableInventoryMapper.convert(i, store, language))
                .toList();

        return createReadableList(availabilities, returnList);
    }

}
