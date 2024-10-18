package com.asrevo.cvhome.store.service.facade.product;

import static com.asrevo.cvhome.store.utils.ReadableEntityUtil.createReadableList;

import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.inventory.PersistableInventory;
import com.asrevo.cvhome.store.core.model.catalog.product.inventory.ReadableInventory;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.availability.ProductAvailabilityService;
import com.asrevo.cvhome.store.core.services.catalog.product.variant.ProductVariantService;
import com.asrevo.cvhome.store.service.mapper.inventory.PersistableInventoryMapper;
import com.asrevo.cvhome.store.service.mapper.inventory.ReadableInventoryMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("productInventoryFacade")
public class ProductInventoryFacadeImpl implements ProductInventoryFacade {

    private final ProductAvailabilityService productAvailabilityService;

    private final ProductService productService;

    private final ProductVariantService productVariantService;

    private final ReadableInventoryMapper readableInventoryMapper;

    private final PersistableInventoryMapper productInventoryMapper;

    public ProductInventoryFacadeImpl(
            ProductAvailabilityService productAvailabilityService,
            ProductService productService,
            ProductVariantService productVariantService,
            ReadableInventoryMapper readableInventoryMapper,
            PersistableInventoryMapper productInventoryMapper) {
        this.productAvailabilityService = productAvailabilityService;
        this.productService = productService;
        this.productVariantService = productVariantService;
        this.readableInventoryMapper = readableInventoryMapper;
        this.productInventoryMapper = productInventoryMapper;
    }

    private void validateProductHasSameStore(MerchantStore store, Product product) {
        if (!product.getMerchantStore().getId().equals(store.getId())) {
            throw new ResourceNotFoundException(
                    "Product with id ["
                            + product.getId()
                            + "] not found for store id ["
                            + store.getInvoiceTemplate()
                            + "]");
        }
    }

    @Override
    public void delete(Long productId, Long inventoryId, MerchantStore store) {
        Optional<ProductAvailability> availability =
                productAvailabilityService.getById(inventoryId, store);
        try {
            if (availability.isPresent()) {
                if (availability.get().getProduct().getId().equals(productId)) {
                    productAvailabilityService.delete(availability.get());
                } else {
                    throw new ResourceNotFoundException(
                            "Product with id ["
                                    + productId
                                    + "] and inventory id ["
                                    + inventoryId
                                    + "] not found for store id ["
                                    + store.getId()
                                    + "]");
                }
            } else {
                throw new ResourceNotFoundException(
                        "Product with id ["
                                + productId
                                + "] and inventory id ["
                                + inventoryId
                                + "] not found for store id ["
                                + store.getId()
                                + "]");
            }
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while deleting inventory", e);
        }
    }

    /*
     * @Override public ReadableInventory get(Long inventoryId, MerchantStore store,
     * Language language) { ProductAvailability availability =
     * getAvailabilityById(store, inventoryId); return
     * readableInventoryMapper.convert(availability, store, language); }
     */

    // public ReadableInventory get(String child, Language language) {
    /*
     * Product product = getProductById(productId); MerchantStore store =
     * getMerchantStore(child);
     *
     * if (isStoreParentNotExist(store) ||
     * store.getParent().getId().equals(product.getMerchantStore().getId())) { throw
     * new ResourceNotFoundException( "MerchantStore [" + child +
     * "] is not a store of retailer [" + store.getCode() + "]"); }
     *
     * ProductAvailability availability =
     * productAvailabilityService.getByStore(product, store) .orElseThrow(() -> new
     * ResourceNotFoundException("Inventory with not found"));
     *
     * return this.readableInventory(availability, store, language);
     */
    // }

    /**
     * private boolean isStoreParentNotExist(MerchantStore store) {
     * return Objects.isNull(store.getParent());
     * }
     * <p>
     * <p>
     * private MerchantStore getMerchantStore(String child) {
     * try {
     * return Optional.ofNullable(merchantStoreService.getByCode(child))
     * .orElseThrow(() -> new ResourceNotFoundException("MerchantStore [" + child + "] not found"));
     * } catch (ServiceException e) {
     * throw new ServiceRuntimeException("Error while getting inventory", e);
     * }
     * }
     * <p>
     * private ReadableInventory readableInventory(ProductAvailability availability, MerchantStore store, Language language) {
     * return readableInventoryMapper.convert(availability, store, language);
     * }
     **/
    private Product getProductById(Long productId, MerchantStore store) {
        return productService
                .retrieveById(productId, store)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Product with id [" + productId + "] not found"));
    }

    private ProductVariant getProductByInstance(Long instanceId, MerchantStore store) {
        return productVariantService
                .getById(instanceId, store)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Product with instance [" + instanceId + "] not found"));
    }

    @Override
    public ReadableInventory add(
            PersistableInventory inventory, MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
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

    private ProductAvailability getProductAvailabilityToSave(
            PersistableInventory inventory, MerchantStore store) {

        return productInventoryMapper.convert(inventory, store, store.getDefaultLanguage());
    }

    @Override
    public ReadableInventory get(Long inventoryId, MerchantStore store, Language language) {

        ProductAvailability availability =
                productAvailabilityService
                        .getById(inventoryId, store)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Inventory with id ["
                                                        + inventoryId
                                                        + "] not found"));
        return readableInventoryMapper.convert(availability, store, language);
    }

    @Override
    public void update(PersistableInventory inventory, MerchantStore store, Language language) {

        Assert.notNull(inventory, "Inventory cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");

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

        ProductAvailability avail =
                originAvailability.stream()
                        .filter(a -> a.getId().longValue() == inventory.getId())
                        .findAny()
                        .orElse(null);

        if (avail == null) {
            throw new ResourceNotFoundException(
                    "Inventory with id [" + inventory.getId() + "] not found");
        }

        inventory.setProductId(product.getId());

        avail = productInventoryMapper.merge(inventory, avail, store, language);
        avail.setProduct(product);
        avail.setMerchantStore(store);
        saveOrUpdate(avail);
    }

    @Override
    public ReadableEntityList<ReadableInventory> get(
            String sku, MerchantStore store, Language language, int page, int count) {
        Assert.notNull(sku, "Product sku cannot be null");
        Assert.notNull(store, "MerchantStore code cannot be null");
        Assert.notNull(language, "Language cannot be null");

        Page<ProductAvailability> availabilities =
                productAvailabilityService.getBySku(sku, page, count);

        if (availabilities.isEmpty()) {
            // get parent product
            try {
                Product singleProduct = productService.getBySku(sku, store);
                if (singleProduct != null) {
                    availabilities =
                            new PageImpl<>(new ArrayList<>(singleProduct.getAvailabilities()));
                }
            } catch (ServiceException e) {
                throw new ServiceRuntimeException(
                        "An error occured while getting product with sku " + sku, e);
            }
        }

        List<ReadableInventory> returnList =
                availabilities.getContent().stream()
                        .map(i -> this.readableInventoryMapper.convert(i, store, language))
                        .collect(Collectors.toList());

        return createReadableList(availabilities, returnList);
    }

    @Override
    public ReadableEntityList<ReadableInventory> get(
            Long productId, MerchantStore store, Language language, int page, int count) {

        Assert.notNull(productId, "Product id cannot be null");
        Assert.notNull(store, "MerchantStore code cannot be null");

        Page<ProductAvailability> availabilities =
                productAvailabilityService.listByProduct(productId, store, page, count);

        List<ReadableInventory> returnList =
                availabilities.getContent().stream()
                        .map(i -> this.readableInventoryMapper.convert(i, store, language))
                        .collect(Collectors.toList());

        return createReadableList(availabilities, returnList);
    }
}
