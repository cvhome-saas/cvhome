package com.asrevo.cvhome.inventory.services.availability;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductAvailability;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ProductAvailabilityService extends SalesManagerEntityService<Long, ProductAvailability> {

    ProductAvailability saveOrUpdate(ProductAvailability availability);

    Page<ProductAvailability> listByProduct(Long productId, StoreMerchantId store, Pageable pageable);

    List<ProductAvailability> listAllByProduct(Long productId, StoreMerchantId store);

    /**
     * Get by sku
     */
    Page<ProductAvailability> getBySku(String sku, Pageable pageable);

    /**
     * All availability rows for a sku and store. Locked — meant for the reservation path.
     */
    List<ProductAvailability> getBySku(String sku, StoreMerchantId store);

    /**
     * Unlocked bulk read for listing/PDP price merges.
     */
    List<ProductAvailability> getBySkus(Collection<String> skus, StoreMerchantId store);

    Optional<ProductAvailability> getById(Long availabilityId, StoreMerchantId store);

}
