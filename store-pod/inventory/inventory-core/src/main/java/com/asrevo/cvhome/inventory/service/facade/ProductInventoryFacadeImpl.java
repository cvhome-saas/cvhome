package com.asrevo.cvhome.inventory.service.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductAvailability;
import com.asrevo.cvhome.inventory.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.inventory.errors.InventoryNotFoundException;
import com.asrevo.cvhome.inventory.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.model.inventory.PersistableInventory;
import com.asrevo.cvhome.inventory.model.inventory.ReadableInventory;
import com.asrevo.cvhome.inventory.service.mapper.PersistableInventoryMapper;
import com.asrevo.cvhome.inventory.service.mapper.ReadableInventoryMapper;
import com.asrevo.cvhome.inventory.services.availability.ProductAvailabilityService;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import static com.asrevo.cvhome.store.utils.ReadableEntityUtil.createReadableList;

@Service("productInventoryFacade")
public class ProductInventoryFacadeImpl implements ProductInventoryFacade {

    private final ProductAvailabilityService productAvailabilityService;

    private final ReadableInventoryMapper readableInventoryMapper;

    private final PersistableInventoryMapper productInventoryMapper;

    public ProductInventoryFacadeImpl(ProductAvailabilityService productAvailabilityService,
                                      ReadableInventoryMapper readableInventoryMapper,
                                      PersistableInventoryMapper productInventoryMapper) {
        this.productAvailabilityService = productAvailabilityService;
        this.readableInventoryMapper = readableInventoryMapper;
        this.productInventoryMapper = productInventoryMapper;
    }

    @Override
    public void delete(Long productId, Long inventoryId, StoreMerchantId store)
            throws InventoryNotFoundException {
        ProductAvailability availability = productAvailabilityService.getById(inventoryId, store)
                .filter(it -> Objects.equals(it.getProductId(), productId))
                .orElseThrow(() -> InventoryNotFoundException.of(inventoryId, store));
        productAvailabilityService.delete(availability);
    }

    @Transactional
    @Override
    public void deleteByProduct(Long productId, StoreMerchantId store) {
        // Orphan cleanup after a catalog product delete — nothing to report if no rows exist.
        for (ProductAvailability availability : productAvailabilityService.listAllByProduct(productId, store)) {
            productAvailabilityService.delete(availability);
        }
    }

    @Override
    public ReadableInventory add(PersistableInventory inventory, StoreMerchantId store, LanguageCode language)
            throws InventoryNotConvertibleException, InventoryReferenceUnresolvableException {
        ProductAvailability availability = productInventoryMapper.convert(inventory, store, language);

        productAvailabilityService.saveOrUpdate(availability);
        return readableInventoryMapper.convert(availability, store, language);
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
            InventoryReferenceUnresolvableException {

        ProductAvailability avail = Optional.ofNullable(inventory.getId())
                .filter(id -> id > 0)
                .flatMap(id -> productAvailabilityService.getById(id, store))
                .orElse(null);
        if (avail == null) {
            throw InventoryNotFoundException.of(inventory.getId(), store);
        }

        avail = productInventoryMapper.merge(inventory, avail, store, language);
        avail.setStoreMerchantId(store);
        productAvailabilityService.saveOrUpdate(avail);
    }

    @Override
    public ReadableEntityList<ReadableInventory> get(String sku, StoreMerchantId store, LanguageCode language,
                                                     Pageable pageable)
            throws InventoryNotConvertibleException {
        Page<ProductAvailability> availabilities = productAvailabilityService.getBySku(sku, pageable);

        return createReadableList(availabilities, toReadable(availabilities, store, language));
    }

    @Override
    public ReadableEntityList<ReadableInventory> get(Long productId, StoreMerchantId store, LanguageCode language,
                                                     Pageable pageable) throws InventoryNotConvertibleException {
        Page<ProductAvailability> availabilities = productAvailabilityService.listByProduct(productId, store, pageable);

        return createReadableList(availabilities, toReadable(availabilities, store, language));
    }

    /**
     * Plain loops rather than {@code stream().map(...)}: the inventory mapper declares a checked failure, and a
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
